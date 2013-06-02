package org.nkvoll.gpsd.client.messages

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.slf4j.LoggerFactory
import org.joda.time.format.ISODateTimeFormat
import java.util.Date
import com.fasterxml.jackson.databind.node.ObjectNode
import org.nkvoll.gpsd.client.messages

object Messages {
  val mapper = new ObjectMapper()
  val logger = LoggerFactory.getLogger(getClass)

  val dateFormat  = ISODateTimeFormat.dateTimeParser()

  def splitMessageParts(data: Array[Byte]): (Option[Array[Byte]], Array[Byte]) = {
    if(data.size <= 2) (None, data)
    else {
      var end = 0
      var bracesCount = 0

      var containsBraces = false

      while(end < data.size && (bracesCount != 0 || !containsBraces)) {
        bracesCount += (data(end) match {
          case '{' => 1
          case '}' => -1
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

  def maybeBoolean(jsonNode: JsonNode) = if(jsonNode.isMissingNode) None else Some(jsonNode.asBoolean())
  def maybeText(jsonNode: JsonNode) = if(jsonNode.isMissingNode) None else Some(jsonNode.asText())
  def maybeDouble(jsonNode: JsonNode) = if(jsonNode.isMissingNode) None else Some(jsonNode.asDouble())
  def maybeInt(jsonNode: JsonNode) = if(jsonNode.isMissingNode) None else Some(jsonNode.asInt())
  def maybeTime(jsonNode: JsonNode): Option[Date] = if(jsonNode.isMissingNode) None else Some(parseDateTime(jsonNode.asText()))

  def parseDateTime(str: String) = {
    try {
      dateFormat.parseDateTime(str).toDate
    } catch {
      case e: IllegalArgumentException => {
        new java.util.Date((java.lang.Double.parseDouble(str) * 1000).asInstanceOf[Long])
      }
    }
  }

  def parseDevice(objectNode: ObjectNode): Device = {
    val activatedDate = parseDateTime(objectNode.path("activated").asText())

    val maybePath = maybeText(objectNode.path("path"))
    val maybeFlag = maybeInt(objectNode.path("flag"))
    val maybeDriver = maybeText(objectNode.path("driver"))
    val maybeSubtype = maybeText(objectNode.path("subtype"))
    val maybeBps = maybeInt(objectNode.path("bps"))

    val maybeNative = maybeInt(objectNode.path("native"))

    val maybeCycle = maybeDouble(objectNode.path("cycle"))
    val maybeMincycle = maybeDouble(objectNode.path("mincycle"))

    Device(maybePath, activatedDate, maybeFlag, maybeDriver, maybeSubtype, maybeBps, objectNode.path("parity").asText, objectNode.path("stopbits").asInt(), maybeNative, maybeCycle, maybeMincycle)
  }

  def parseDevices(objectNode: ObjectNode): Devices = {
    var devices = List[Device]()

    val it = objectNode.path("devices").iterator()
    while(it.hasNext) devices ::= parseDevice(it.next().asInstanceOf[ObjectNode])

    Devices(devices)
  }

  def parseTPV(objectNode: ObjectNode): TPV = {
    val maybeTag = maybeText(objectNode.get("tag"))
    val maybeDevice = maybeText(objectNode.path("device"))
    val maybeTime_ = maybeTime(objectNode.path("time"))
    val maybeEpt = maybeDouble(objectNode.path("ept"))

    val maybeLat = maybeDouble(objectNode.path("lat"))
    val maybeLon = maybeDouble(objectNode.path("lon"))
    val maybeAlt = maybeDouble(objectNode.path("alt"))

    val maybeEpx = maybeDouble(objectNode.path("epx"))
    val maybeEpy = maybeDouble(objectNode.path("epy"))
    val maybeEpv = maybeDouble(objectNode.path("epv"))

    val maybeTrack = maybeDouble(objectNode.path("track"))
    val maybeSpeed = maybeDouble(objectNode.path("speed"))
    val maybeClimb = maybeDouble(objectNode.path("climb"))

    val maybeEpd = maybeDouble(objectNode.path("epd"))
    val maybeEps = maybeDouble(objectNode.path("eps"))
    val maybeEpc = maybeDouble(objectNode.path("epc"))

    TPV(objectNode.path("mode").asInt(),
      maybeTag, maybeDevice, maybeTime_, maybeEpt,

      maybeLat, maybeLon, maybeAlt,

      maybeEpx, maybeEpy, maybeEpv,

      maybeTrack, maybeSpeed, maybeClimb,

      maybeEpd, maybeEps, maybeEpc)
  }

  def parseSatellite(objectNode: ObjectNode): Satellite = {
    Satellite(objectNode.path("PRN").asInt(), objectNode.path("el").asInt(), objectNode.path("az").asInt(), objectNode.path("ss").asInt(), objectNode.path("used").asBoolean())
  }

  def parseSky(objectNode: ObjectNode): Sky = {
    var satellites = List[Satellite]()

    val it = objectNode.path("satellites").iterator()
    while(it.hasNext) satellites ::= parseSatellite(it.next().asInstanceOf[ObjectNode])

    val maybeTag = maybeText(objectNode.path("tag"))
    val maybeDevice = maybeText(objectNode.path("device"))
    val maybeTime_ = maybeTime(objectNode.path("time"))

    val maybeXdop = maybeDouble(objectNode.path("xdop"))
    val maybeYdop = maybeDouble(objectNode.path("ydop"))
    val maybeVdop = maybeDouble(objectNode.path("vdop"))
    val maybeTdop = maybeDouble(objectNode.path("tdop"))
    val maybeHdop = maybeDouble(objectNode.path("hdop"))
    val maybePdop = maybeDouble(objectNode.path("pdop"))
    val maybeGdop = maybeDouble(objectNode.path("gdop"))

    Sky(maybeTag, maybeDevice, maybeTime_, maybeXdop, maybeYdop, maybeVdop, maybeTdop, maybeHdop, maybePdop, maybeGdop, satellites)
  }

  def parseVersion(objectNode: ObjectNode): Version = {
    val maybeRemote = maybeText(objectNode.path("remote"))

    Version(objectNode.path("release").asText, objectNode.path("rev").asText(), objectNode.path("proto_major").asInt, objectNode.path("proto_minor").asInt, maybeRemote)
  }

  def parseWatch(objectNode: ObjectNode): messages.Watch = {
    val maybeEnable = maybeBoolean(objectNode.path("enable"))
    val maybeJson = maybeBoolean(objectNode.path("json"))
    val maybeNmea = maybeBoolean(objectNode.path("nmea"))

    val maybeRaw = maybeInt(objectNode.path("raw"))
    val maybeScaled = maybeBoolean(objectNode.path("scaled"))

    val maybeDevice = maybeText(objectNode.path("device"))
    val maybeRemote = maybeText(objectNode.path("remote"))

    val maybeTiming = maybeBoolean(objectNode.path("timing"))


    messages.Watch(maybeEnable, maybeJson, maybeNmea, maybeRaw, maybeScaled, maybeDevice, maybeRemote, maybeTiming)
  }

  def parseMessage(data: Array[Byte]): Option[GPSMessage] = {
    logger.debug(s"Parsing: ${new String(data)}")

    val objectNode = mapper.readValue(data, classOf[ObjectNode])

    objectNode.path("class").asText() match {
      case "DEVICE" => {
        Some(parseDevice(objectNode))
      }
      case "DEVICES" => {
        Some(parseDevices(objectNode))
      }
      case "TPV" => {
        Some(parseTPV(objectNode))
      }
      case "SKY" => {
        Some(parseSky(objectNode))
      }
      case "VERSION" => {
        Some(parseVersion(objectNode))
      }
      case "WATCH" => {
        Some(parseWatch(objectNode))
      }
      case unknown => {
        logger.warn(s"Received unknown message: ${new String(data)}")
        None
      }
    }
  }
}
