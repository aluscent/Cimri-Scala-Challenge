package com.cimri

import com.cimri.FileHandler.{getCsvFile, getJsonFile}
import com.cimri.MongoWrapper.{insertManyIntoMongo, insertOneIntoMongo}
import com.cimri.model.Feed
import org.mongodb.scala.SingleObservable
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.result.InsertManyResult

import java.text.SimpleDateFormat
import java.util.Date

object FileProcessor {

  def splitFeedsDayByDay(listOfFeed: List[Feed]): Map[String, List[Feed]] = listOfFeed
    .groupBy(feed => new SimpleDateFormat("yyyy-MM-dd").format(feed.version))

  def insertOneFile(path: String, feed: Feed): Unit = {
    val version = "version" -> BsonDateTime(feed.version)

    val fileToDocument = getCsvFile(s"$path/${feed.name}")
      .map { record =>
        Document(record.map { case (key, value) => key -> value }) + version
      }

    insertManyIntoMongo(fileToDocument, "price_changes")
    Thread.sleep(1000)
  }

  def processBaseAndInsertIntoMongoByDay(path: String, listOfFeed: List[Feed]): Unit = {

    val base = listOfFeed.head
    val deltas = listOfFeed.tail

    if (base.feedType != "base" || !deltas.forall(_.feedType == "delta"))
      throw new Exception("Multiple or none base file")

    listOfFeed
      .foreach(feed => insertOneFile(path, feed))
  }

  def main(args: Array[String]): Unit = {

    val listOfFeed = getJsonFile("/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge/feeds.json")
    val feedByDay = splitFeedsDayByDay(listOfFeed)
    println(feedByDay)

//    processBaseAndInsertIntoMongoByDay("/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge", listOfFeed)

  }
}
