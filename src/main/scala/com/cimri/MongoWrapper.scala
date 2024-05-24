package com.cimri

import cats.effect.{Async, IO, Resource}
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.{InsertManyResult, InsertOneResult}

import java.util.Date
import scala.concurrent.Future
import scala.language.higherKinds

object MongoWrapper {
  def apply(collectionName: String): Resource[IO, MongoWrapper] =
    Resource.make(IO(new MongoWrapper {
      override def collection: MongoCollection[Document] = cimriDB.getCollection(collectionName)
    }))(src => IO(src.closeConnection()))
}

abstract class MongoWrapper {

  private def mongoClient = MongoClient("mongodb://localhost:27017/cimri")
  def cimriDB: MongoDatabase = mongoClient.getDatabase("cimri")
  def collection: MongoCollection[Document]

  def closeConnection(): Unit = mongoClient.close()

  private def observer[T]: Observer[T] = new Observer[T] {
    override def onNext(result: T): Unit = println("Done")

    override def onError(e: Throwable): Unit = println(s"Failed. Reason: ${e.getMessage}")

    override def onComplete(): Unit = println("Completed")
  }

  // Commands
  import cats.syntax.applicative._

  def insertOneIntoMongo(document: Document): IO[Unit] = collection
.insertOne(document).subscribe(observer[InsertOneResult]).pure[IO]

  def insertManyIntoMongo(documents: Seq[_ <: Document]): IO[Unit] = collection
.insertMany(documents).subscribe(observer[InsertManyResult]).pure[IO]

  // Queries

  import scala.concurrent.ExecutionContext
  implicit val ec: ExecutionContext = ExecutionContext.global

  def queryById(id: String): IO[Option[Document]] =
    Async[IO] fromFuture collection.find(equal("id", id)).first().headOption().pure[IO]

  def queryByIdAndDate(id: String, startDate: Date, endDate: Date): IO[Seq[Document]] =
    Async[IO] fromFuture collection.find(and(
      equal("id", id),
      gte("version", BsonDateTime(startDate)),
      lte("version", BsonDateTime(endDate))
    )).toFuture().pure[IO]

}
