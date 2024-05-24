package com.cimri

import cats.effect.{Async, IO}
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.{InsertManyResult, InsertOneResult}

import java.util.Date
import scala.concurrent.Future
import scala.language.higherKinds

class MongoWrapper(collectionName: String) {

  import cats.syntax.applicative._

  private lazy val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017/cimri")
  private lazy val cimriDB: MongoDatabase = mongoClient.getDatabase("cimri")
  private lazy val collection: MongoCollection[Document] = cimriDB.getCollection(collectionName)

  private def observer[T]: Observer[T] = new Observer[T] {
    override def onNext(result: T): Unit = println("Done")

    override def onError(e: Throwable): Unit = println(s"Failed. Reason: ${e.getMessage}")

    override def onComplete(): Unit = println("Completed")
  }

  // Commands
  def insertOneIntoMongo(document: Document): IO[Unit] = collection
    .insertOne(document)
    .subscribe(observer[InsertOneResult])
    .pure[IO]

  def insertManyIntoMongo(documents: Seq[_ <: Document]): IO[Unit] = collection
    .insertMany(documents)
    .subscribe(observer[InsertManyResult])
    .pure[IO]

  // Queries

  import scala.concurrent.ExecutionContext

  implicit val ec: ExecutionContext = ExecutionContext.global

  private def futureToIo[T, S[_]](future: Future[S[T]]): IO[S[T]] =
    Async[IO] fromFuture (Async[IO] delay future)

  def queryById(id: String): IO[Option[Document]] = futureToIo(
    collection
    .find(equal("id", id))
    .first()
    .headOption()
  )

  def queryByIdAndDate(id: String, startDate: Date, endDate: Date): IO[Seq[Document]] = futureToIo(
    collection
      .find(and(
        equal("id", id),
        gte("version", BsonDateTime(startDate)),
        lte("version", BsonDateTime(endDate))
      ))
      .toFuture()
  )

}
