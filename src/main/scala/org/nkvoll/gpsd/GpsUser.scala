package org.nkvoll.gpsd

import akka.actor.{ActorRef, Actor}
import akka.io.Tcp
import akka.util.ByteString
import org.slf4j.LoggerFactory

class GpsUser(locationActor: ActorRef) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)
  val enableWatchMessage = ByteString("?WATCH={\"enable\":true,\"json\":true}")
  val disableWatchMessage = ByteString("?WATCH={\"enable\":false}")

  val OPENING_BRACE = '{'.asInstanceOf[Byte]
  val CLOSING_BRACE = '}'.asInstanceOf[Byte]

  var client: ActorRef = _

  var dataBuffer = ByteString()

  var lastLocationUpdate = 0L
  val locationUpdateFrequency = 10 * 10e9

  def extractMessageData(data: ByteString): (Option[ByteString], ByteString) = {
    if(data.size <= 2) (None, data)
    else {
      var end = 0
      var bracesCount = 0

      var containsBraces = false

      while(end < data.size && (bracesCount != 0 || !containsBraces)) {
        bracesCount += (data(end) match {
          case OPENING_BRACE => 1
          case CLOSING_BRACE => -1
          case _ => 0
        })

        containsBraces = containsBraces || bracesCount != 0

        end += 1
      }

      if(bracesCount == 0 && end != 0 && containsBraces)
        (Some(data.take(end)), data.drop(end))
      else
        (None, data)
    }
  }

  def receive = {
    case Tcp.Connected => {}
    case receivedData: ByteString => {
      var msgData = extractMessageData(this.dataBuffer ++ receivedData)

      //logger.info(s"Got data: $msgData")

      while(msgData._1.isDefined) {
        val message = Messages.parseMessage(msgData._1.get.toArray)

        logger.info(s"Got message: $message")

        message match {
          case Some(Version(cls, release, rev, protoMajor, protoMinor, remote)) => {
            sender ! enableWatchMessage
          }
          case Some(tvp: TPV) => {
            if(lastLocationUpdate + locationUpdateFrequency < System.nanoTime()) {
              lastLocationUpdate = System.nanoTime()

              val locationOption = for {
                lat <- tvp.lat
                lon <- tvp.lon
              } yield Location(lat, lon)

              locationOption match {
                case Some(location) => {
                  logger.debug(s"Looking up location: $location using $locationActor")
                  locationActor ! location
                }
                case None => ()
              }
            }
          }
          case _ => ()
        }

        msgData = extractMessageData(msgData._2)
      }
      this.dataBuffer = msgData._2
    }
  }

  override def postStop() {
    if(client != null) client ! disableWatchMessage
  }
}
