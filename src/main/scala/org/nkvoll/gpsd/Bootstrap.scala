package org.nkvoll.gpsd

import akka.actor.{Props, ActorSystem}
import akka.io.{IO, Tcp}
import com.typesafe.config.ConfigFactory

object Bootstrap extends App {
  implicit val system = ActorSystem("hello-gpsd")

  val config = ConfigFactory.load().getConfig("org.nkvoll.gpsd")
  val lookupConfig = config.getConfig("lookup")

  val lookupActor = system.actorOf(Props(classOf[LookupCoordinates], lookupConfig), "lookup-coordinates")
  val gpsUserActor = system.actorOf(Props(classOf[GpsUser], lookupActor), "gps-user")
  val clientActor = system.actorOf(Props(classOf[Client], config.getString("host"), config.getInt("port"), gpsUserActor), "client")

  //lookupActor ! Location(60.255639198, 6.15)
}
