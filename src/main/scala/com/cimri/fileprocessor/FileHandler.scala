package com.cimri.fileprocessor

import cats.effect.{IO, Resource}
import cats.syntax.parallel._
import com.cimri.model.{Feed, FeedsFile}
import spray.json._

import scala.io.Source

/**
 * Tools used for reading and handling files
 */
object FileHandler {

  /**
   * Reads CSV files that contains price changes.
   * Creates a Resource to manage the file.
   * @param path the path to the feed file
   * @return a list of Mapped feed that maps each data entry to its corresponding column name
   */
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

  /**
   * Reads the JSON file that contains list of feed files.
   * Creates a Resource to manage the file.
   * @param path the path to the feed file
   * @return list of all feed files
   */
  def getJsonFile(path: String): IO[List[Feed]] = Resource
    .make(IO(Source.fromFile(path)("UTF-8")))(src => IO(src.close()))
    .use(file => IO(file.mkString.parseJson.convertTo[FeedsFile].feeds))
}
