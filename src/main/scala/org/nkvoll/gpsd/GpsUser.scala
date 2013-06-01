package org.nkvoll.gpsd

import akka.actor.{ActorRef, Actor}
import akka.io.Tcp
import akka.util.ByteString
import org.slf4j.LoggerFactory
import spray.json._
import MessagesJsonProtocol._

class GpsUser extends Actor {
  val logger = LoggerFactory.getLogger(getClass)
  val enableWatchMessage = ByteString("?WATCH={\"enable\":true,\"json\":true}")
  val disableWatchMessage = ByteString("?WATCH={\"enable\":false}")

  var client: ActorRef = _

  def receive = {
    case Tcp.Connected => {}
    case data: ByteString => {
      logger.debug(s"user got data: ${data.utf8String}")

      val banner = data.utf8String.asJson.convertTo[ServerBanner]
      logger.debug(s"Server banner received: ${banner}")

      sender ! enableWatchMessage

      context become {
        case data: ByteString => {
          logger.info(s"user got data: ${data.utf8String.trim}")
          data.utf8String.
          val message = data.utf8String.asJson.convertTo[GPSMessage]
          logger.info(s"Got message: $message")
        }
      }
    }
  }

  override def postStop() {
    if(client != null) client ! disableWatchMessage
  }
}
