package org.nkvoll.gpsd

import akka.actor.{ActorRef, Actor}
import akka.io.{IO, Tcp}
import java.net.InetSocketAddress
import org.slf4j.{LoggerFactory, Logger}
import akka.util.ByteString

class Client(host: String, port: Int, listener: ActorRef, manager: ActorRef) extends Actor {
  val logger = LoggerFactory.getLogger(getClass)

  import context.system

  logger.info(s"Connecting to $host:$port using $manager")

  IO(Tcp) ! Tcp.Connect(new InetSocketAddress(host, port))

  var connection: ActorRef = _

  def receive = {
    case connected @ Tcp.Connected(remoteAddr, localAddr) => {
      logger.info(s"Connected to $remoteAddr via $localAddr")

      connection = sender
      sender ! Tcp.Register(self)

      listener ! connected

      context become connectedReceive
    }
    case failed @ Tcp.CommandFailed(cmd) => {
      listener ! failed
      context.stop(self)
    }
    case a => {
      logger.warn("Received unexpected message: " + a)
    }
  }

  def connectedReceive: Receive = {
    case Tcp.Received(data) => {
      logger.debug(s"Client $this received: ${data.utf8String}")
      listener ! data
    }
    case data: ByteString => {
      logger.info(s"Writing data: $data")
      connection ! Tcp.Write(data)
    }
    case a => {
      logger.warn("Received connected " +a)
    }
  }
}
