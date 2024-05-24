package com.cimri

import cats.effect.{ExitCode, IO, IOApp}
import com.cimri.FileHandler.{getCsvFile, getJsonFile}
import com.cimri.model.Feed
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.collection.immutable.Document

import java.text.SimpleDateFormat
import java.util.Date
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object FileProcessor extends IOApp {

  def splitFeedsDayByDay(listOfFeed: List[Feed]): Map[String, List[Feed]] = listOfFeed
    .groupBy(feed => new SimpleDateFormat("yyyy-MM-dd").format(feed.version))

  def insertOneFile(mongo: MongoWrapper, path: String, feed: Feed): IO[Unit] = {
    val version = "version" -> BsonDateTime(feed.version)

    getCsvFile(s"$path/${feed.name}")
      .map(_.map { record =>
        Document(record.map { case (key, value) => key -> value }) + version
      })
      .flatMap(mongo.insertManyIntoMongo)
  }

  def processBaseAndInsertIntoMongoByDay(mongo: MongoWrapper, path: String, listOfFeed: List[Feed]): IO[List[Unit]] = {

    val base = listOfFeed.head
    val deltas = listOfFeed.tail

    if (base.feedType != "base" || !deltas.forall(_.feedType == "delta"))
      throw new Exception("Multiple or none base file")

    import cats.syntax.parallel._
    listOfFeed
      .parTraverse(feed => insertOneFile(mongo, path, feed))
  }

  override def run(args: List[String]): IO[ExitCode] = {

    val listOfFeed = getJsonFile("/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge/feeds.json")

    val dirPath = "/home/aluscent/IdeaProjects/Cimri-Scala-Challenge/cimri-challenge"
    val mongoWrapper = new MongoWrapper("price_changes")

    //    listOfFeed.flatMap(list => processBaseAndInsertIntoMongoByDay(mongoWrapper, dirPath, list)) >>
    //      IO.sleep(1 second) >>
    val start = new SimpleDateFormat("yyyy-MM-dd").parse("2024-03-26")
    val end = new SimpleDateFormat("yyyy-MM-dd").parse("2024-03-28")

    mongoWrapper.queryByIdAndDate("92233720367378670", start, end).map(println) >>
      IO(ExitCode.Success)
  }
}
