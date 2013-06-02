package org.nkvoll.gpsd.client.messages

import org.scalatest.{Inside, FunSpec}
import org.scalatest.matchers.ShouldMatchers
import java.util.{Calendar, GregorianCalendar}

class MessagesSpec extends FunSpec with ShouldMatchers with Inside {
  describe("Messages") {
    it("should not split on invalid or incomplete messages") {
      val invalidDatas = List("{foo", "", "\r\nfoobar}").map(_.getBytes)
      invalidDatas.foreach(invalidData => {
        val (msgOpt, rest) = Messages.splitMessageParts(invalidData)

        msgOpt should be (None)
        rest should be (invalidData)
      })
    }

    it("should split on complete messages") {
      val datas = List("{foo}", "\r\n{bar}").map(_.getBytes)
      datas.foreach(data => {
        val (msgOpt, rest) = Messages.splitMessageParts(data)

        inside(msgOpt) { case Some(msgData) =>
          msgData should be (data)
        }
        rest should be (Array.empty[Byte])
      })
    }

    it("should split on complete messages, leaving additional data behind") {
      val dataAndRests = List(("{foo}", "remaining"), ("\r\n{bar}", "{more")).map(x => (x._1.getBytes, x._2.getBytes))
      dataAndRests.foreach { case (data, rest) => {
        val (msgOpt, msgRest) = Messages.splitMessageParts(data ++ rest)

        inside(msgOpt) { case Some(msgData) =>
          msgData should be (data)
        }
        msgRest should be (rest)
      }}
    }

    it("should be able to parse VERSION messages") {
      val data = """{"class":"VERSION","release":"2.93","rev":"2010-03-30T12:18:17","proto_major":3,"proto_minor":2}""".getBytes()

      inside(Messages.parseMessage(data)) { case Some(Version(release, rev, major, minor, remote)) => {
        release should be ("2.93")
        rev should be ("2010-03-30T12:18:17")
        major should be (3)
        minor should be (2)

        remote should be (None)
      }}
    }

    it("should be able to parse VERSION messages with a remote") {
      val data = """{"class":"VERSION","release":"2.93","rev":"2010-03-30T12:18:17","proto_major":3,"proto_minor":2,"remote":"foobar"}""".getBytes()

      inside(Messages.parseMessage(data)) { case Some(Version(release, rev, major, minor, remote)) => {
        release should be ("2.93")
        rev should be ("2010-03-30T12:18:17")
        major should be (3)
        minor should be (2)
        remote should be (Some("foobar"))
      }}
    }

    it("should be able to parse WATCH messages") {
      val data = """{"class":"WATCH","enable":true,"json":true,"nmea":false,"raw":0,"scaled":false,"timing":false}""".getBytes

      inside(Messages.parseMessage(data)) { case Some(Watch(enableOpt, jsonOpt, nmeaOpt, rawOpt, scaledOpt, deviceOpt, remoteOpt, timingOpt)) => {
        enableOpt should be (Some(true))
        jsonOpt should be (Some(true))
        nmeaOpt should be (Some(false))
        rawOpt should be (Some(0))
        scaledOpt should be (Some(false))
        timingOpt should be (Some(false))
        remoteOpt should be (None)
      }}
    }

    it("should be able to parse DEVICES messages") {
      val data = """{"class":"DEVICES","devices":[{"class":"DEVICE","path":"/dev/ttyUSB0","activated":1269959537.20,"native":0,"bps":4800,"parity":"N","stopbits":1,"cycle":1.00}]}""".getBytes

      val cal = new GregorianCalendar(2010, 2, 30, 14, 32, 17)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 200)

      inside(Messages.parseMessage(data)) { case Some(Devices(devices)) => {
        inside(devices) { case Seq(Device(pathOpt, activated, flagOpt, driverOpt, subtypeOpt, bpsOpt, parity, stopBits, nativeOpt, cycleOpt, mincycleOpt)) => {
          pathOpt should be (Some("/dev/ttyUSB0"))
          activated.getTime should be === (cal.getTime.getTime)
          flagOpt should be (None)
          driverOpt should be (None)
          subtypeOpt should be (None)
          bpsOpt should be (Some(4800))
          parity should be ("N")
          stopBits should be (1)
          nativeOpt should be (Some(0))
          cycleOpt should be (Some(1.0))
          mincycleOpt should be (None)
        }}
      }}
    }

    it("should be able to parse DEVICE messages") {
      val data = """{"class":"DEVICE","path":"/dev/ttyUSB0","activated":1269960793.97,
                   |                 "driver":"SiRF binary","native":1,"bps":4800,
                   |                 "parity":"N","stopbits":1,"cycle":1.00}""".stripMargin.getBytes

      val cal = new GregorianCalendar(2010, 2, 30, 14, 53, 13)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 970)

      inside(Messages.parseMessage(data)) { case Some(Device(pathOpt, activated, flagOpt, driverOpt, subtypeOpt, bpsOpt, parity, stopBits, nativeOpt, cycleOpt, mincycleOpt)) => {
        pathOpt should be (Some("/dev/ttyUSB0"))
        activated.getTime should be === (cal.getTime.getTime)
        flagOpt should be (None)
        driverOpt should be (Some("SiRF binary"))
        subtypeOpt should be (None)
        bpsOpt should be (Some(4800))
        parity should be ("N")
        stopBits should be (1)
        nativeOpt should be (Some(1))
        cycleOpt should be (Some(1.0))
        mincycleOpt should be (None)
      }}
    }

    it("should be able to parse TPV mode 1 messages") {
      val data = """{"class":"TPV","tag":"MID2","device":"/dev/ttyUSB0",
                   |               "time":"2010-04-30T11:47:43.28Z","ept":0.005,"mode":1}""".stripMargin.getBytes

      val cal = new GregorianCalendar(2010, 3, 30, 11, 47, 43)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 280)

      inside(Messages.parseMessage(data)) { case Some(TPV(mode, tagOpt, deviceOpt, timeOpt, eptOpt, latOpt, lonOpt, altOpt, epxOpt, epyOpt, epvOpt, trackOpt, speedOpt, climbOpt, epdOpd, epsOpt, epcOpt)) => {
        mode should be (1)
        tagOpt should be (Some("MID2"))
        deviceOpt should be (Some("/dev/ttyUSB0"))
        timeOpt.get.getTime should be === (cal.getTime.getTime)
        eptOpt should be (Some(0.005))
        latOpt should be (None)
        lonOpt should be (None)
        altOpt should be (None)
        epxOpt should be (None)
        epyOpt should be (None)
        epvOpt should be (None)
        trackOpt should be (None)
        speedOpt should be (None)
        climbOpt should be (None)
        epdOpd should be (None)
        epsOpt should be (None)
        epcOpt should be (None)
      }}
    }

    it("should be able to parse TPV mode 2 messages") {
      val data = """  {
                   |    "class":"TPV",
                   |    "tag":"MID2",
                   |    "device":"/dev/ttyUSB0",
                   |    "time":1253593085.470,
                   |    "ept":0.005,
                   |    "lat":38.88945123,
                   |    "lon":-77.03522143,
                   |    "track":171.7249,
                   |    "speed":0.467,
                   |    "mode":2
                   |  }""".stripMargin.getBytes

      val cal = new GregorianCalendar(2009, 8, 22, 4, 18, 5)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 470)

      inside(Messages.parseMessage(data)) { case Some(TPV(mode, tagOpt, deviceOpt, timeOpt, eptOpt, latOpt, lonOpt, altOpt, epxOpt, epyOpt, epvOpt, trackOpt, speedOpt, climbOpt, epdOpd, epsOpt, epcOpt)) => {
        mode should be (2)
        tagOpt should be (Some("MID2"))
        deviceOpt should be (Some("/dev/ttyUSB0"))
        timeOpt.get.getTime should be === (cal.getTime.getTime)
        eptOpt should be (Some(0.005))
        latOpt should be (Some(38.88945123))
        lonOpt should be (Some(-77.03522143))
        altOpt should be (None)
        epxOpt should be (None)
        epyOpt should be (None)
        epvOpt should be (None)
        trackOpt should be (Some(171.7249))
        speedOpt should be (Some(0.467))
        climbOpt should be (None)
        epdOpd should be (None)
        epsOpt should be (None)
        epcOpt should be (None)
      }}
    }

    it("should be able to parse TPV mode 3 messages") {
      val data = """{"class":"TPV","tag":"MID2","time":"2010-04-30T11:48:20.10Z","ept":0.005,
                   |               "lat":46.498204497,"lon":7.568061439,"alt":1327.689,
                   |                "epx":15.319,"epy":17.054,"epv":124.484,"track":10.3797,
                   |                "speed":0.091,"climb":-0.085,"eps":34.11,"mode":3}""".stripMargin.getBytes

      val cal = new GregorianCalendar(2010, 3, 30, 11, 48, 20)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 100)

      inside(Messages.parseMessage(data)) { case Some(TPV(mode, tagOpt, deviceOpt, timeOpt, eptOpt, latOpt, lonOpt, altOpt, epxOpt, epyOpt, epvOpt, trackOpt, speedOpt, climbOpt, epdOpd, epsOpt, epcOpt)) => {
        mode should be (3)
        tagOpt should be (Some("MID2"))
        deviceOpt should be (None)
        timeOpt.get.getTime should be === (cal.getTime.getTime)
        eptOpt should be (Some(0.005))
        latOpt should be (Some(46.498204497))
        lonOpt should be (Some(7.568061439))
        altOpt should be (Some(1327.689))
        epxOpt should be (Some(15.319))
        epyOpt should be (Some(17.054))
        epvOpt should be (Some(124.484))
        trackOpt should be (Some(10.3797))
        speedOpt should be (Some(0.091))
        climbOpt should be (Some(-0.085))
        epdOpd should be (None)
        epsOpt should be (Some(34.11))
        epcOpt should be (None)
      }}
    }

    it("should be able to parse SKY messages") {
      val data = """{"class":"SKY","tag":"MID2","device":"/dev/pts/1",
                   |    "time":"2005-07-08T11:28:07.114Z",
                   |    "xdop":1.55,"hdop":1.24,"pdop":1.99,
                   |    "satellites":[
                   |        {"PRN":28,"el":7,"az":160,"ss":0,"used":false},
                   |        {"PRN":8,"el":66,"az":189,"ss":44,"used":true}]}""".stripMargin.getBytes

      val cal = new GregorianCalendar(2005, 6, 8, 11, 28, 7)
      cal.setTimeZone(java.util.TimeZone.getTimeZone("UTC"))
      cal.set(Calendar.MILLISECOND, 114)

      inside(Messages.parseMessage(data)) { case Some(Sky(tagOpt, deviceOpt, timeOpt, xdopOpt, ydopOpt, vdopOpt, tdopOpt, hdopOpt, pdopOpt, gdopOpt, satelittes)) => {
        tagOpt should be (Some("MID2"))
        deviceOpt should be (Some("/dev/pts/1"))
        timeOpt.get.getTime should be === (cal.getTime.getTime)
        xdopOpt should be (Some(1.55))
        ydopOpt should be (None)
        vdopOpt should be (None)
        tdopOpt should be (None)
        hdopOpt should be (Some(1.24))
        pdopOpt should be (Some(1.99))
        gdopOpt should be (None)

        inside(satelittes) { case Seq(s2: Satellite, s1: Satellite) => {
          inside(s1) { case Satellite(prn, el, az, ss, used) => {
            prn should be (28)
            el should be (7)
            az should be (160)
            ss should be (0)
            used should be (false)
          }}

          inside(s2) { case Satellite(prn, el, az, ss, used) => {
            prn should be (8)
            el should be (66)
            az should be (189)
            ss should be (44)
            used should be (true)
          }}
        }}
      }}
    }
  }
}
