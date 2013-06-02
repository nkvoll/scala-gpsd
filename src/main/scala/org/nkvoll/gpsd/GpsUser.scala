package org.nkvoll.gpsd

import akka.actor.{ActorRef, Actor}
import akka.util.ByteString
import org.slf4j.LoggerFactory
import org.nkvoll.gpsd.client.{commands, messages}
import org.nkvoll.gpsd.location.Location


class GpsUser(locationActor: ActorRef) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)

  var dataBuffer = ByteString()

  var lastLocationUpdate = 0L
  val locationUpdateFrequency = 15 * 1000

  def receive = {
    case messages.Version(cls, release, rev, protoMajor, protoMinor, remote) => {
      sender ! commands.Watch(enable=true, json=true)
    }
    case tvp: messages.TPV => {
      if(lastLocationUpdate + locationUpdateFrequency < System.currentTimeMillis()) {
        lastLocationUpdate = System.currentTimeMillis()

        for {
          lat <- tvp.lat
          lon <- tvp.lon
        } yield locationActor ! Location(lat, lon)
      }
    }
    case message: messages.GPSMessage => {
      logger.info(s"Received gps message: $message")
    }
    case otherwise => {
      logger.info(s"Received ignored message: $otherwise")
    }
  }
}
