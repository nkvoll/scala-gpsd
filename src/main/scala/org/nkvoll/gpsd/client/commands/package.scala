package org.nkvoll.gpsd.client

import com.fasterxml.jackson.databind.ObjectMapper

package object commands {

  sealed trait GPSCommand {
    def serialize(): Array[Byte]
  }

  object GPSCommand {
    val mapper = new ObjectMapper()

    val watchPrefix = "?WATCH=".getBytes
  }

  case class Watch(enable: Boolean, json: Boolean) extends GPSCommand {
    def serialize(): Array[Byte] = {
      val node = GPSCommand.mapper.createObjectNode()

      node.put("enable", enable)

      if(json) node.put("json", json)

      GPSCommand.watchPrefix ++ GPSCommand.mapper.writeValueAsBytes(node)
    }
  }
}
