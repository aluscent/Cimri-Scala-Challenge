package com.cimri.implicits

import com.cimri.model.{Feed, FeedsFile}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat, deserializationError}

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.Try

object FeedImplicits extends DefaultJsonProtocol {
  implicit object DateFormat extends RootJsonFormat[Date] {
    def write(date: Date): JsValue = JsString(dateToIsoString(date))

    def read(json: JsValue): Date = json match {
      case JsString(rawDate) => parseIsoDateString(rawDate)
      case error => deserializationError(s"Expected JsString, got $error")
    }

    private def dateToIsoString(date: Date): String = date.toString

    private def parseIsoDateString(date: String): Date =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX").parse(date)
  }


  implicit val feedFormat: RootJsonFormat[Feed] = jsonFormat3(Feed)

  implicit val feedFileFormat: RootJsonFormat[FeedsFile] = jsonFormat1(FeedsFile)

  implicit val ordering: Ordering[Feed] = Ordering
    .by(_.version)
}