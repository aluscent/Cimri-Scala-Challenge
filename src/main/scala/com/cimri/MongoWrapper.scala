package com.cimri

import org.mongodb.scala._
import org.mongodb.scala.result.{InsertManyResult, InsertOneResult}

object MongoWrapper {
  private val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017/cimri")
  private val cimriDB: MongoDatabase = mongoClient.getDatabase("cimri")

  private def getCollection(collectionName: String): MongoCollection[Document] = collectionName match {
    case "base_prices" => cimriDB.getCollection("base_prices")
    case "price_changes" => cimriDB.getCollection("price_changes")
    case _ => throw new Exception("No collection with this name!")
  }

  def insertOneIntoMongo(document: Document, collectionName: String): Unit =
    getCollection(collectionName)
      .insertOne(document)
      .subscribe(new Observer[InsertOneResult] {
        override def onNext(result: InsertOneResult): Unit = println("Inserted")
        override def onError(e: Throwable): Unit     = println(s"Failed. Reason: ${e.getMessage}")
        override def onComplete(): Unit              = println("Completed")
      })

  def insertManyIntoMongo(documents: Seq[_ <: Document], collectionName: String): Unit =
    getCollection(collectionName)
      .insertMany(documents)
      .subscribe(new Observer[InsertManyResult] {
        override def onNext(result: InsertManyResult): Unit = println("Inserted")
        override def onError(e: Throwable): Unit     = println(s"Failed. Reason: ${e.getMessage}")
        override def onComplete(): Unit              = println("Completed")
      })

}
