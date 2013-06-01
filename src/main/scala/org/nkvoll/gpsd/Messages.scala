package org.nkvoll.gpsd

import java.util.Date
import spray.json._
import java.text.SimpleDateFormat
import scala.math.BigDecimal

/*{"class":"VERSION","release":"2.93","rev":"2010-03-30T12:18:17",
"proto_major":3,"proto_minor":2}*/
case class ServerBanner(cls: String, release: String, revision: String, protoMajor: Int, protoMinor: Int)


sealed trait GPSMessage

sealed trait TPV extends GPSMessage

/*{"class":"TPV","tag":"MID2","device":"/dev/ttyUSB0",
               "time":"2010-04-30T11:47:43.28Z","ept":0.005,"mode":1}*/

case class TPVMode1(tag: String, device: String, time: Date, ept: Float) extends TPV

/*{"class":"TPV","tag":"MID2","time":"2010-04-30T11:48:20.10Z","ept":0.005,
               "lat":46.498204497,"lon":7.568061439,"alt":1327.689,
                "epx":15.319,"epy":17.054,"epv":124.484,"track":10.3797,
                "speed":0.091,"climb":-0.085,"eps":34.11,"mode":3}
*/
case class TPVMode3(tag: String, device: String, time: Date, ept: Float, lat: Float, lon: Float, alt: Float, epx: Float, epy: Float, epv: Float, track: Float, speed: Float, climb: Float, eps: Float) extends TPV

/*{"class":"DEVICES","devices":[{"class":"DEVICE","path":"/dev/ttyUSB0","activated":"2013-06-01T10:23:04.823Z","flags":1,"driver":"SiRF binary","subtype":"GSW3.5.0_3.5.00.00-SDK-3EP2.01 ","native":1,"bps":4800,"parity":"N","stopbits":1,"cycle":1.00}]}*/
case class Device(path: String, activated: Date, flag: Int, driver: String, subtype: String, native: Int, bps: Int, parity: String, stopBits: Int, cycle: Float)
case class Devices(devices: Device*)

/*{"class":"WATCH","enable":true,"json":true,"nmea":false,"raw":0,"scaled":false,"timing":false}*/
case class Watch(enable: Boolean, json: Boolean, nmea: Boolean, raw: Int, scaled: Boolean, timing: Boolean)

object MessagesJsonProtocol extends DefaultJsonProtocol {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")

  implicit object GPSMessageFormat extends RootJsonReader[GPSMessage] {
    def read(json: JsValue): GPSMessage = {
      val jsonObject = json.asJsObject

      jsonObject.fields.get("class") match {
        case Some(JsString("WATCH")) => jsonObject.getFields("enable", "json", "nmea", "raw", "scaled", "timing") match {
          case Seq(JsBoolean(enable), JsBoolean(json), JsBoolean(nmea), JsNumber(raw), JsBoolean(scaled), JsBoolean(timing)) => {
            Watch(enable, json, nmea, raw.toInt, scaled, timing)
          }
        }
        case Some(JsString("TPV")) => {
          jsonObject.fields.get("mode") match {
            case Some(JsNumber(n)) if n == 1 => jsonObject.getFields("tag", "device", "time", "ept") match {
              case Seq(JsString(tag), JsString(device), JsString(time), JsNumber(ept)) => {
                TPVMode1(tag, device, dateFormat.parse(time.stripSuffix("Z")), ept.toFloat)
              }
              case _ => deserializationError("Unable to decode a TPV mode 1 message")
            }
            case Some(JsNumber(n)) if n == 3 => jsonObject.getFields("tag", "device", "time", "ept", "lat", "lon", "alt", "epx", "epy", "epv", "track", "speed", "climb", "eps") match {
              case Seq(JsString(tag), JsString(device), JsString(time), JsNumber(ept), JsNumber(lat), JsNumber(lon), JsNumber(alt), JsNumber(epx), JsNumber(epy), JsNumber(epv), JsNumber(track), JsNumber(speed), JsNumber(climb), JsNumber(eps)) => {
                TPVMode3(tag, device, dateFormat.parse(time.stripSuffix("Z")), ept.toFloat, lat.toFloat, lon.toFloat, alt.toFloat, epx.toFloat, epy.toFloat, epv.toFloat, track.toFloat, speed.toFloat, climb.toFloat, eps.toFloat)
              }
              case _ => deserializationError("Unable to decode a TPV mode 3 message")
            }
            case _ => deserializationError("Unable to decode a TPV message (unknown mode)")
          }
        }
        case _ => deserializationError("Unable to decode a gps message")
      }
    }
  }

  implicit object ServerBannerFormat extends RootJsonReader[ServerBanner] {
    def read(json: JsValue): ServerBanner = {
      json.asJsObject.getFields("class", "release", "rev", "proto_major", "proto_minor") match {
        case Seq(JsString(cls), JsString(rel), JsString(rev), JsNumber(maj), JsNumber(min)) => {
          ServerBanner(cls, rel, rev, maj.toInt, min.toInt)
        }
        case _ => deserializationError("Unable to read banner.")
      }
    }
  }
}