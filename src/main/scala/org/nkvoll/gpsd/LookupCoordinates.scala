package org.nkvoll.gpsd

import org.slf4j.LoggerFactory
import akka.actor.Actor
import scalikejdbc._
import scalikejdbc.SQLInterpolation._
import com.typesafe.config.{Config, ConfigFactory}


case class Location(lat: Double, lon: Double)

case class City(gid: Int, iso: String, name: String, asciiName: String, latitude: Double, longitude: Double, timezone: String, population: Int, elevation: Int, alternateNames: String, featureClass: String, featureCode: String, cc2: String, admin1Code: String, admin2Code: String, admin3Code: String, admin4Code: String, dem: Int, updated: String)

object City extends SQLSyntaxSupport[City] {
  override val tableName = "cities"

  def tryParseInt(str: String): Int = try {
    Integer.parseInt(str)
  } catch {
    case e: Throwable => -1
  }

  def apply(p: SyntaxProvider[City])(rs: WrappedResultSet): City = apply(p.resultName)(rs)
  def apply(p: ResultName[City])(rs: WrappedResultSet): City = City(
    rs.int(p.gid), rs.string(p.iso), rs.string(p.name), rs.string(4), rs.double(p.latitude), rs.double(p.longitude), rs.string(p.timezone), rs.int(p.population), rs.stringOpt(p.elevation).map(tryParseInt).get, rs.string(10), rs.string(11), rs.string(12), rs.string(p.cc2), rs.string(14), rs.string(15), rs.string(16), rs.string(17), rs.int(p.dem), rs.string(p.updated)
  )
}

class LookupCoordinates(config: Config) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)

  Class.forName(config.getString("driverClass"))

  ConnectionPool.singleton(config.getString("connectionUrl"), "", "")
  val c = City.syntax("c")

  val offset = 0.15

  def receive = {
    case location @ Location(lat, lon) => {
      logger.info(s"Started looking up location $location")
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

        val numClosest = 3
        val sortedCities = cities.sortBy(city => math.pow(lat - city.latitude, 2) + math.pow(lon - city.longitude, 2)).take(numClosest)
        val sortedString = sortedCities.mkString("\n", "\n", "")

        logger.info(s"Expected $numClosest closest cities: $sortedString")
      }}
    }
    case message => {
      logger.info(s"Lookup received %message")
    }
  }
}
