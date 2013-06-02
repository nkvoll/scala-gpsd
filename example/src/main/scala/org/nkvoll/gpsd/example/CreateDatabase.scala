package org.nkvoll.gpsd.example

import sys.process._
import com.typesafe.config.ConfigFactory
import scalikejdbc._
import java.sql.DriverManager
import scala.io.{BufferedSource, Source}
import collection.JavaConverters._
import java.io.PrintWriter
import org.slf4j.LoggerFactory

object CreateDatabase {
  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {
    val config = ConfigFactory.load().getConfig("org.nkvoll.gpsd.lookup")

    Class.forName(config.getString("driver-class"))

    logger.info(s"Using database: ${config.getString("connection-url")}")

    using(DB(DriverManager.getConnection(config.getString("connection-url"), "", ""))) { db =>

      db autoCommit { implicit session =>
        SQL(
          """
            |CREATE TABLE IF NOT EXISTS cities (    "gid" INTEGER NOT NULL UNIQUE ,
            |    "iso" TEXT,
            |    "name" TEXT,
            |    "asciiname" TEXT,
            |    "latitude" REAL,
            |    "longitude" REAL,
            |    "timezone" TEXT,
            |    "population" INTEGER,
            |    "elevation" INTEGER,
            |    "alternate_names" TEXT,
            |    "feature_class" TEXT,
            |    "feature_code" TEXT,
            |    "cc2" TEXT,
            |    "admin1_code" TEXT,
            |    "admin2_code" TEXT,
            |    "admin3_code" TEXT,
            |    "admin4_code" TEXT,
            |    "dem" INTEGER,
            |    "updated" TEXT);
          """.stripMargin).execute()()

        args.foreach(filename => {
          val input = getInput(filename)
          var i = 0

          input.getLines().foreach(inputLine => {
            i += 1
            if(i%1000 == 0) logger.info(s"Inserting entry number $i")

            SQL(
              """insert or replace into cities
                | (gid, iso, name, asciiname, latitude, longitude, timezone, population, elevation, alternate_names,
                | feature_class, feature_code, cc2, admin1_code, admin2_code, admin3_code, admin4_code, dem, updated)
                | values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              """.stripMargin).bind(inputLine.split('\t'): _*).update()()
          })

          logger.info(s"Added or updated $i entries.")
        })

        SQL(
          """
            |CREATE INDEX IF NOT EXISTS cities_latitude_index on cities(latitude);
            |CREATE INDEX IF NOT EXISTS cities_longitude_index on cities(longitude);
          """.stripMargin).execute()()

        logger.info("Done")
      }
    }
  }

  def getInput(filename: String): BufferedSource = {
    val file = if(filename.startsWith("http")) {
      val filenameParts = filename.split('.')
      val tempFile = java.io.File.createTempFile("download-", "." + filenameParts(filenameParts.length-1))
      tempFile.deleteOnExit()

      logger.info(s"Downloading $filename")
      val output = (new java.net.URL(filename) #> tempFile).!!
      logger.info(s"Downloaded for a total of ${tempFile.length()} bytes. $output")

      tempFile
    } else new java.io.File(filename.replace("~",System.getProperty("user.home")))

    if(filename.endsWith("zip")) {
      val zipfile = new java.util.zip.ZipFile(file)
      zipfile.entries().asScala.filterNot(_.getName.contains("readme")).foreach(entry => {
        return Source.fromInputStream(zipfile.getInputStream(entry))
      })
      null
    } else {
      Source.fromFile(file)
    }
  }
}
