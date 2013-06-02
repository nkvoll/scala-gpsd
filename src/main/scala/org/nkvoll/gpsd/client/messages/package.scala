package org.nkvoll.gpsd.client

import java.util.Date
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.slf4j.LoggerFactory
import org.joda.time.format.ISODateTimeFormat
import akka.util.ByteString
import com.fasterxml.jackson.databind.node.ObjectNode

package object messages {

  sealed trait GPSMessage

  /**
   * Name	Always?	Type	Description
    class	Yes	string	Fixed: "VERSION"
    release	Yes	string	Public release level
    rev	Yes	string	Internal revision-control level.
    proto_major	Yes	numeric	API major revision level.
    proto_minor	Yes	numeric	API minor revision level.
    remote	No	string	URL of the remote daemon reporting this version. If empty, this is the version of the local daemon.

   */
  case class Version(`class`: String, release: String, rev: String, protoMajor: Int, protoMinor: Int, remote: Option[String]) extends GPSMessage

  /**
   *
  Name	Always?	Type	Description
    class	Yes	string	Fixed: "TPV"
    tag	No	string	Type tag associated with this GPS sentence; from an NMEA device this is just the NMEA sentence type.
    device	No	string	Name of originating device.
    mode	Yes	numeric	NMEA mode: %d, 0=no mode value yet seen, 1=no fix, 2=2D, 3=3D.
    time	No	string	Time/date stamp in ISO8601 format, UTC. May have a fractional part of up to .001sec precision. May be absent if mode is not 2 or 3.
    ept	No	numeric	Estimated timestamp error (%f, seconds, 95% confidence). Present if time is present.
    lat	No	numeric	Latitude in degrees: +/- signifies West/East. Present when mode is 2 or 3.
    lon	No	numeric	Longitude in degrees: +/- signifies North/South. Present when mode is 2 or 3.
    alt	No	numeric	Altitude in meters. Present if mode is 3.
    epx	No	numeric	Longitude error estimate in meters, 95% confidence. Present if mode is 2 or 3 and DOPs can be calculated from the satellite view.
    epy	No	numeric	Latitude error estimate in meters, 95% confidence. Present if mode is 2 or 3 and DOPs can be calculated from the satellite view.
    epv	No	numeric	Estimated vertical error in meters, 95% confidence. Present if mode is 3 and DOPs can be calculated from the satellite view.
    track	No	numeric	Course over ground, degrees from true north.
    speed	No	numeric	Speed over ground, meters per second.
    climb	No	numeric	Climb (positive) or sink (negative) rate, meters per second.
    epd	No	numeric	Direction error estimate in degrees, 95% confidence.
    eps	No	numeric	Speed error estinmate in meters/sec, 95% confidence.
    epc	No	numeric	Climb/sink error estimate in meters/sec, 95% confidence.
   */
  case class TPV(mode: Int, tag: Option[String], device: Option[String], time: Option[Date], ept: Option[Double], lat: Option[Double], lon: Option[Double], alt: Option[Double], epx: Option[Double], epy: Option[Double], epv: Option[Double], track: Option[Double], speed: Option[Double], climb: Option[Double], epd: Option[Double], eps: Option[Double], epc: Option[Double]) extends GPSMessage

  /**
   * Name	Always?	Type	Description
    class	Yes	string	Fixed: "DEVICE"
    path	No	string	Name the device for which the control bits are being reported, or for which they are to be applied. This attribute may be omitted only when there is exactly one subscribed channel.
    activated	No	string	Time the device was activated as an ISO8601 timestamp. If the device is inactive this attribute is absent.
    flags	No	integer	Bit vector of property flags. Currently defined flags are: describe packet types seen so far (GPS, RTCM2, RTCM3, AIS). Won't be reported if empty, e.g. before gpsd has seen identifiable packets from the device.
    driver	No	string	GPSD's name for the device driver type. Won't be reported before gpsd has seen identifiable packets from the device.
    subtype	When the daemon sees a delayed response to a probe for subtype or firmware-version information.	string	Whatever version information the device returned.
    bps	No	integer	Device speed in bits per second.
    parity	Yes	string	N, O or E for no parity, odd, or even.
    stopbits	Yes	string	Stop bits (1 or 2).
    native	No	integer	0 means NMEA mode and 1 means alternate mode (binary if it has one, for SiRF and Evermore chipsets in particular). Attempting to set this mode on a non-GPS device will yield an error.
    cycle	No	real	Device cycle time in seconds.
    mincycle	No	real	Device minimum cycle time in seconds. Reported from ?CONFIGDEV when (and only when) the rate is switchable. It is read-only and not settable.
   */
  case class Device(path: Option[String], activated: Date, flag: Option[Int], driver: Option[String], subtype: Option[String], bps: Option[Int], parity: String, stopBits: Int, native: Option[Int], cycle: Option[Double], mincycle: Option[Double]) extends GPSMessage

  case class Devices(devices: Device*) extends GPSMessage

  /**
   * Name	Always?	Type	Description
    PRN	Yes	numeric	PRN ID of the satellite. 1-63 are GNSS satellites, 64-96 are GLONASS satellites, 100-164 are SBAS satellites
    az	Yes	numeric	Azimuth, degrees from true north.
    el	Yes	numeric	Elevation in degrees.
    ss	Yes	numeric	Signal strength in dB.
    used	Yes	boolean	Used in current solution? (SBAS/WAAS/EGNOS satellites may be flagged used if the solution has corrections from them, but not all drivers make this information available.)

   */
  case class Satellite(prn: Int, el: Int, az: Int, ss: Int, used: Boolean)

  /**
   * Name	Always?	Type	Description
    class	Yes	string	Fixed: "SKY"
    tag	No	string	Type tag associated with this GPS sentence; from an NMEA device this is just the NMEA sentence type.
    device	No	string	Name of originating device
    time	No	numeric	Time/date stamp in ISO8601 format, UTC. May have a fractional part of up to .001sec precision.
    xdop	No	numeric	Longitudinal dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    ydop	No	numeric	Latitudinal dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    vdop	No	numeric	Altitude dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    tdop	No	numeric	Time dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    hdop	No	numeric	Horizontal dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get a circular error estimate.
    pdop	No	numeric	Spherical dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    gdop	No	numeric	Hyperspherical dilution of precision, a dimensionless factor which should be multiplied by a base UERE to get an error estimate.
    satellites	Yes	list	List of satellite objects in skyview
   */
  case class Sky(tag: Option[String], device: Option[String], time: Option[Date], xdop: Option[Double], ydop: Option[Double], vdop: Option[Double], tdop: Option[Double], hdop: Option[Double], pdop: Option[Double], gdop: Option[Double], satellites: Satellite*) extends GPSMessage


  /**
   * Name	Always?	Type	Description
    class	Yes	string	Fixed: "WATCH"
    enable	No	boolean	Enable (true) or disable (false) watcher mode. Default is true.
    json	No	boolean	Enable (true) or disable (false) dumping of JSON reports. Default is false.
    nmea	No	boolean	Enable (true) or disable (false) dumping of binary packets as pseudo-NMEA. Default is false.
    raw	No	integer	Controls 'raw' mode. When this attribute is set to 1 for a channel, gpsd reports the unprocessed NMEA or AIVDM data stream from whatever device is attached. Binary GPS packets are hex-dumped. RTCM2 and RTCM3 packets are not dumped in raw mode. When this attribute is set to 2 for a channel that processes binary data, gpsd reports the received data verbatim without hex-dumping.
    scaled	No	boolean	If true, apply scaling divisors to output before dumping; default is false. Applies only to AIS and Subframe reports.
    device	No	string	If present, enable watching only of the specified device rather than all devices. Useful with raw and NMEA modes in which device responses aren't tagged. Has no effect when used with enable:false.
    remote	No	string	URL of the remote daemon reporting the watch set. If empty, this is a WATCH response from the local daemon.
   */
  case class Watch(enable: Option[Boolean], json: Option[Boolean], nmea: Option[Boolean], raw: Option[Int], scaled: Option[Boolean], device: Option[String], remote: Option[String], timing: Option[Boolean]) extends GPSMessage


  object Messages {
    val mapper = new ObjectMapper()
    val logger = LoggerFactory.getLogger(getClass)

    val dateFormat  = ISODateTimeFormat.dateTimeParser()

    val OPENING_BRACE = '{'.asInstanceOf[Byte]
    val CLOSING_BRACE = '}'.asInstanceOf[Byte]

    def splitMessageParts(data: ByteString): (Option[ByteString], ByteString) = {
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
    def maybeTime(jsonNode: JsonNode): Option[Date] = if(jsonNode.isMissingNode) None else Some(dateFormat.parseDateTime(jsonNode.asText()).toDate)

    def parseDevice(objectNode: ObjectNode): Device = {
      val activatedDate = dateFormat.parseDateTime(objectNode.path("activated").asText()).toDate

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

      Devices(devices: _*)
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
      Satellite(objectNode.path("prn").asInt(), objectNode.path("el").asInt(), objectNode.path("az").asInt(), objectNode.path("ss").asInt(), objectNode.path("used").asBoolean())
    }

    def parseSky(objectNode: ObjectNode): Sky = {
      var satellites = List[Satellite]()

      val it = objectNode.path("devices").iterator()
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

      Sky(maybeTag, maybeDevice, maybeTime_, maybeXdop, maybeYdop, maybeVdop, maybeTdop, maybeHdop, maybePdop, maybeGdop, satellites: _*)
    }

    def parseVersion(objectNode: ObjectNode): Version = {
      val maybeRemote = maybeText(objectNode.path("remote"))

      Version(objectNode.path("class").asText, objectNode.path("release").asText, objectNode.path("rev").asText(), objectNode.path("proto_major").asInt, objectNode.path("proto_minor").asInt, maybeRemote)
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
}