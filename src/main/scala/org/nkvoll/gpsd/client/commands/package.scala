package org.nkvoll.gpsd.client

import akka.util.ByteString
import com.fasterxml.jackson.databind.ObjectMapper

package object commands {
  sealed trait GPSCommands {
    def serialize(): ByteString
  }

  object GPSCommands {
    val mapper = new ObjectMapper()

    val watchPrefix = "?WATCH=".getBytes
  }

  case class Watch(enable: Boolean, json: Boolean) extends GPSCommands {
    def serialize(): ByteString = {
      val node = GPSCommands.mapper.createObjectNode()

      node.put("enable", enable)

      if(json) node.put("json", json)

      ByteString(GPSCommands.watchPrefix ++ GPSCommands.mapper.writeValueAsBytes(node))
    }
  }
}
