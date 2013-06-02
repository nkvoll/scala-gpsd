package org.nkvoll.gpsd

import akka.actor.{Props, ActorSystem}
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory
import org.nkvoll.gpsd.client.{RetryTimer, GPSClient}
import org.nkvoll.gpsd.location.LookupLocation
import scalikejdbc.ConnectionPool

object Bootstrap extends App {
  implicit val system = ActorSystem("hello-gpsd")

  val config = ConfigFactory.load().getConfig("org.nkvoll.gpsd")
  val lookupConfig = config.getConfig("lookup")

  // prepare database
  Class.forName(lookupConfig.getString("driverClass"))
  ConnectionPool.singleton(lookupConfig.getString("connectionUrl"), "", "")

  val lookupActor = system.actorOf(Props[LookupLocation], "lookup-location")
  val gpsUserActor = system.actorOf(Props(classOf[GpsUser], lookupActor), "gps-user")
  //val clientActor = system.actorOf(Props(classOf[GPSClient], config.getString("host"), config.getInt("port"), gpsUserActor), "client")
  val clientActor = system.actorOf(Props(classOf[GPSClient], config.getString("host"), config.getInt("port"), gpsUserActor, RetryTimer.getDefaultRetryTimer), "client")

}
