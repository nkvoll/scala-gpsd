package org.nkvoll.gpsd.example.location

import org.slf4j.LoggerFactory
import akka.actor.Actor
import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import com.typesafe.config.{Config, ConfigFactory}


class LookupLocation(selectOffset: Float, numClosest: Int) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)
  val c = PartialCity.syntax("c")

  def receive = {
    case location @ Location(lat, lon) => {
      logger.info(s"Started looking up location $location")
      val start = System.currentTimeMillis()
      DB readOnly { implicit session =>
        val cities = withSQL {
          select
            .from(PartialCity as c)
            .where
              .lt(c.latitude, lat + selectOffset)
              .and.gt(c.latitude, lat - selectOffset)
              .and.lt(c.longitude, lon + selectOffset)
              .and.gt(c.longitude, lon - selectOffset)
            .limit(100)
        }.map(PartialCity(c)).list()()

        val queryTook = System.currentTimeMillis() - start

        // calculate manhattan distance for now:
        val sortedCities = cities.sortBy(city => math.pow(lat - city.latitude, 2) + math.pow(lon - city.longitude, 2)).take(numClosest)
        val sortedString = sortedCities.map(c => (c.name, c.latitude, c.longitude)).mkString("    - ", "\n    - ", "")

        val allTook = System.currentTimeMillis() - start

        logger.info(s"Expected ${math.min(numClosest, cities.size)} (of ${cities.size}) closest cities (in $queryTook ms/$allTook ms):\n$sortedString")
      }
    }
    case message => {
      logger.info(s"Lookup received %message")
    }
  }
}
