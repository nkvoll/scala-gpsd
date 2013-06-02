package org.nkvoll.gpsd.example

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import org.nkvoll.gpsd.client.akka.{RetryTimer, GPSClient}
import org.nkvoll.gpsd.example.location.LookupLocation
import scalikejdbc.ConnectionPool


object Bootstrap extends App {
  implicit val system = ActorSystem("hello-gpsd")

  val config = ConfigFactory.load().getConfig("org.nkvoll.gpsd")
  val lookupConfig = config.getConfig("lookup")

  // prepare database
  Class.forName(lookupConfig.getString("driver-class"))
  ConnectionPool.singleton(lookupConfig.getString("connection-url"), "", "")

  val lookupActor = system.actorOf(Props(classOf[LookupLocation], lookupConfig.getDouble("select-offset").asInstanceOf[Float], lookupConfig.getInt("num-closest")), "lookup-location")
  val gpsUserActor = system.actorOf(Props(classOf[GpsUser], lookupActor), "gps-user")
  val clientActor = system.actorOf(Props(classOf[GPSClient], config.getString("host"), config.getInt("port"), gpsUserActor, RetryTimer.getDefaultRetryTimer), "client")

}
