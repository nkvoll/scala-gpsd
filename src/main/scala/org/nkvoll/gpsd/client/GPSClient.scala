package org.nkvoll.gpsd.client

import akka.actor.{ActorRef, Actor}
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import org.slf4j.LoggerFactory
import akka.io.Tcp.{Connect, CommandFailed}
import akka.util.ByteString
import org.nkvoll.gpsd.client.messages.Messages
import org.nkvoll.gpsd.client.commands.GPSCommands
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

class GPSClient(host: String, port: Int, listener: ActorRef, retryTimer: RetryTimer = RetryTimer.getDefaultRetryTimer) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)

  import context.system
  import context.dispatcher

  def connect() {
    logger.info(s"Connecting to $host:$port.")
    IO(Tcp) ! Tcp.Connect(new InetSocketAddress(host, port))
  }

  override def preStart() {
    connect()
  }

  def receive = {
    case connected @ Tcp.Connected(remoteAddr, localAddr) => {
      logger.info(s"Connected to $remoteAddr via $localAddr")

      retryTimer.resetDelay()

      var buffer = ByteString()
      val connection = sender

      sender ! Tcp.Register(self)
      listener ! connected

      context become {
        case Tcp.Received(data) => {
          var msgData = Messages.splitMessageParts(buffer ++ data)
          while(msgData._1.isDefined) {
            Messages.parseMessage(msgData._1.get.toArray).map(listener ! _)
            msgData = Messages.splitMessageParts(msgData._2)
          }
          buffer = msgData._2
        }
        case closed: Tcp.ConnectionClosed => {
          context unbecome()
          reconnect(s"Connection closed")
        }
        case closeCommand: Tcp.CloseCommand => {
          connection ! closeCommand
        }
        case command: GPSCommands => {
          connection ! Tcp.Write(command.serialize())
        }
        case cf: CommandFailed => listener ! cf
        case a => {
          logger.warn("Received connected " +a)
        }
      }
    }
    case Tcp.CommandFailed(cmd) => {
      reconnect(s"${cmd.failureMessage}")
    }
  }

  def reconnect(msg: String) {
    val duration = Duration(retryTimer.getDelayAndIncrementRetries, TimeUnit.MILLISECONDS)
    logger.info(s"$msg: Reconnecting $self in $duration")
    context.system.scheduler.scheduleOnce(duration)(connect())
  }
}
