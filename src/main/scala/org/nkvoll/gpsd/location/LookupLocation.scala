package org.nkvoll.gpsd.location

import org.slf4j.LoggerFactory
import akka.actor.Actor
import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import com.typesafe.config.{Config, ConfigFactory}




class LookupLocation extends Actor {
  val logger = LoggerFactory.getLogger(getClass)
  val c = City.syntax("c")

  val offset = (1/60f * 2) // search two minutes around the current location (1 minute = 1.852 km)

  def receive = {
    case location @ Location(lat, lon) => {
      logger.info(s"Started looking up location $location")
      val start = System.currentTimeMillis()
      DB readOnly {implicit session => {
        val cities = withSQL {
          select
            .from(City as c)
            .where
              .lt(c.latitude, lat + offset)
              .and.gt(c.latitude, lat - offset)
              .and.lt(c.longitude, lon + offset)
              .and.gt(c.longitude, lon - offset)
            .limit(100)
        }.map(City(c)).list()()

        logger.debug(s"Got cities: $cities")

        // calculate manhattan distance for now:

        val queryTook = System.currentTimeMillis() - start

        val numClosest = 3
        val sortedCities = cities.sortBy(city => math.pow(lat - city.latitude, 2) + math.pow(lon - city.longitude, 2)).take(numClosest)
        val sortedString = sortedCities.mkString("\n", "\n", "")

        val allTook = System.currentTimeMillis() - start

        logger.info(s"Expected $numClosest closest cities (in $queryTook/$allTook): $sortedString")
      }}
    }
    case message => {
      logger.info(s"Lookup received %message")
    }
  }
}
