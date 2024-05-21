package com.cimri

import com.cimri.model.{Feed, FeedsFile}

import java.io.File
import spray.json._

object FileHandler {

  def getCsvFile(path: String): List[Map[String, String]] = {
    val file = scala.io.Source
      .fromFile(new File(path))

    val listFile = file
      .getLines()
      .toList

    val columns = listFile.head.split(",").toList
    val rawFile = listFile.tail.map(_.split(",").toList)

    file.close()

    rawFile.map(l => (columns zip l).toMap)
  }

  def getJsonFile(path: String): List[Feed] = {
    val file = scala.io.Source
      .fromFile(path)("UTF-8")

    val jsFile = file
      .mkString
      .parseJson

    file.close()

    import com.cimri.implicits.FeedImplicits._
    jsFile
      .convertTo[FeedsFile]
      .feeds
  }
}
