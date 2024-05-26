package com.cimri

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.cimri.fileprocessor.FileHandler.{getCsvFile, getJsonFile}
import com.cimri.MainFileProcessor.splitFeedDayByDay
import com.cimri.model.Feed
import org.scalatest.flatspec.AnyFlatSpec

import java.text.SimpleDateFormat
import java.util.Date

class FileProcessorTester extends AnyFlatSpec {

  val listOfFeed: IO[List[Feed]] =
    getJsonFile("/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge/feeds.json")

  val dirPath = "/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge"

  "getJsonFile" should "get a file list" in {
    assert(
      listOfFeed.unsafeRunSync().toString() ==
        "List(Feed(base,base_1.csv,Wed Mar 27 20:46:53 IRST 2024), " +
          "Feed(delta,delta_1.csv,Wed Mar 27 20:51:53 IRST 2024), " +
          "Feed(delta,delta_2.csv,Wed Mar 27 20:56:53 IRST 2024))"
    )
  }

  val groupedFeed: Map[String, List[Feed]] = splitFeedDayByDay(listOfFeed.unsafeRunSync())

  "splitFeedsDayByDay" should "split feed and group them by their day of version" in {
    def stringToDate(date: String): Date =
      new SimpleDateFormat("yyyy-MM-dd HH:mm:ss X").parse(date)

    assert(
      groupedFeed.toString() ==
        Map("2024-03-27" -> List(
          Feed("base", "base_1.csv", stringToDate("2024-03-27 17:16:53 +00:00")),
          Feed("delta", "delta_1.csv", stringToDate("2024-03-27 17:21:53 +00:00")),
          Feed("delta", "delta_2.csv", stringToDate("2024-03-27 17:26:53 +00:00"))
        )).toString()
    )
  }

  "getCsvFile" should "read CSV files containing price changes logs" in {
    val firstCsvFileName = groupedFeed.head._2.head.name

    val sample = getCsvFile(s"$dirPath/$firstCsvFileName").unsafeRunSync().slice(0, 1)

    assert(
      sample.toString() ==
        List(Map(
          "id" -> "9223372034617287039",
          "price" -> "58259.03",
          "url" -> "https://cimri.com/cep-telefonlari/en-ucuz-apple-iphone-15-plus-5g-128gb-mavi-fiyatlari,2237488768",
          "stock" -> "29"
        )).toString()
    )
  }
}
