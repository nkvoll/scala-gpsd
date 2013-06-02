package org.nkvoll.gpsd.location

import scalikejdbc.SQLInterpolation._
import scalikejdbc.WrappedResultSet

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