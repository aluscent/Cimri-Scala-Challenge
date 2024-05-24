package com.cimri

import cats.effect.{ExitCode, IO, IOApp}
import com.cimri.FileHandler.{getCsvFile, getJsonFile}
import com.cimri.model.Feed
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.collection.immutable.Document

import java.text.SimpleDateFormat
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object FileProcessor extends IOApp {

  def splitFeedsDayByDay(listOfFeed: List[Feed]): Map[String, List[Feed]] = listOfFeed
    .groupBy(feed => new SimpleDateFormat("yyyy-MM-dd").format(feed.version))

  def insertOneFile(mongo: MongoWrapper, path: String, feed: Feed): IO[Unit] = {
    val version = "version" -> BsonDateTime(feed.version)

    for {
      file <- getCsvFile(s"$path/${feed.name}")
      documentList = file.map { record =>
        Document(record.map { case (key, value) => key -> value }) + version
      }
      _ <- mongo.insertManyIntoMongo(documentList)
    } yield ()
  }

  def processBaseAndInsertIntoMongoByDay(path: String, listOfFeed: List[Feed])(mongo: MongoWrapper): IO[List[Unit]] = {

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

    for {
      list <- listOfFeed
      _ <- MongoWrapper("price_changes").use(processBaseAndInsertIntoMongoByDay(dirPath, list))
      _ <- IO.sleep(1 seconds)
    } yield ExitCode.Success
  }
}
