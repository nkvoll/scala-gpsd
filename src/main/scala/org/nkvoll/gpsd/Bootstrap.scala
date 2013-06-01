package org.nkvoll.gpsd

import akka.actor.{Props, ActorSystem}
import akka.io.{IO, Tcp}

object Bootstrap extends App {
  implicit val system = ActorSystem("hello-gpsd")
  val manager = IO(Tcp)

  val host = "localhost"
  val port = 2947

  val gpsUserActor = system.actorOf(Props[GpsUser], "gps-user")

  val clientActor = system.actorOf(Props(classOf[Client], host, port, gpsUserActor, manager), "client")
}
