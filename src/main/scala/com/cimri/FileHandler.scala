package com.cimri

import cats.effect.{IO, Resource}
import cats.syntax.parallel._
import com.cimri.model.{Feed, FeedsFile}
import spray.json._

import scala.io.Source

object FileHandler {

  def getCsvFile(path: String): IO[List[Map[String, String]]] = Resource
    .make(IO(Source.fromFile(path)("UTF-8")))(src => IO(src.close()))
    .use { file =>
      val listFile = file
        .getLines()
        .toList

      val columns = listFile.head.split(",").toList
      val rawFile = listFile.tail.map(_.split(",").toList)

      rawFile.map(l => (columns zip l).toMap)
      rawFile.parTraverse(line => IO((columns zip line).toMap))
    }


  import com.cimri.implicits.FeedImplicits._

  def getJsonFile(path: String): IO[List[Feed]] = Resource
    .make(IO(Source.fromFile(path)("UTF-8")))(src => IO(src.close()))
    .use(file => IO(file.mkString.parseJson.convertTo[FeedsFile].feeds))
}
