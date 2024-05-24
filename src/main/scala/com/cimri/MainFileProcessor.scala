package com.cimri

import cats.effect.{ExitCode, IO, IOApp}
import com.cimri.fileprocessor.FileHandler.{getCsvFile, getJsonFile}
import com.cimri.db.MongoWrapper
import com.cimri.model.Feed
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.collection.immutable.Document

import java.text.SimpleDateFormat
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

/**
 * Main entry point for the application file processing part.
 */
object MainFileProcessor extends IOApp {

  /**
   * Splits feed files by day.
   * Useful in combination with [[processBaseAndInsertIntoMongoByDay]].
   * @param listOfFeed list of the entries related to the feed files
   * @return A map of date to a list of all respecting feed file entries
   */
  def splitFeedsDayByDay(listOfFeed: List[Feed]): Map[String, List[Feed]] = listOfFeed
    .groupBy(feed => new SimpleDateFormat("yyyy-MM-dd").format(feed.version))


  /**
   * Inserting one file by its name and path
   * @param mongo the MongoDB interface
   * @param path the path to feed file
   * @param feed the entry related to this feed file
   */
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

  /**
   * Inserting data related to each day of feed into MongoDB.
   * It checks for the data to be exactly for one day.
   * @param path the path to feed file
   * @param listOfFeed list of the entries related to the feed files
   * @param mongo the mongo interface
   */
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
      _ <- MongoWrapper("mongodb://localhost:27017/cimri", "cimri", "price_changes").use(processBaseAndInsertIntoMongoByDay(dirPath, list))
      _ <- IO.sleep(1 seconds)
    } yield ExitCode.Success
  }
}
