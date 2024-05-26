package com.cimri

import cats.effect._
import cats.effect.unsafe.implicits.global
import com.cimri.db.MongoWrapper
import com.dimafeng.testcontainers.{ForAllTestContainer, MongoDBContainer}
import io.circe.Json
import org.http4s.circe.CirceEntityCodec._
import org.http4s.implicits._
import org.http4s.{Method, Request}
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.bson.collection.immutable.Document
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class APIIntegrationSpec extends AnyWordSpec with Matchers with ForAllTestContainer {

  override val container: MongoDBContainer = MongoDBContainer()

  "API" should {
    "retrieve a document by id" in {
      val mongoWrapper: Resource[IO, MongoWrapper] =
        MongoWrapper(container.container.getConnectionString, "cimri", "testCollection")

      // Insert a document to test
      val doc = Document("id" -> "1", "version" -> BsonDateTime(System.currentTimeMillis()))

      mongoWrapper use { mongo =>
        mongo.insertOneIntoMongo(doc).unsafeRunSync()
        val httpApp = api.Endpoints.endpoints(mongo).orNotFound
        val request = Request[IO](method = Method.GET, uri = uri"/api/getById/1")
        val response = httpApp.run(request).unsafeRunSync()

        response.status shouldBe org.http4s.Status.Ok
        val responseBody = response.as[Json].unsafeRunSync()
        responseBody.hcursor.downField("id").as[String] shouldBe Right("1")

        IO.unit
      }
    }

    "retrieve documents by id and date range" in {
      val mongoWrapper: Resource[IO, MongoWrapper] =
        MongoWrapper(container.container.getConnectionString, "cimri", "testCollection")

      // Insert documents to test
      val startDate = new java.util.Date()
      val endDate = new java.util.Date(startDate.getTime + 86400000L) // +1 day
      val doc1 = Document("id" -> "1", "version" -> BsonDateTime(startDate))
      val doc2 = Document("id" -> "1", "version" -> BsonDateTime(endDate))
      val uri = uri"/api/getByIdAndDate".withQueryParams(Map(
        "id" -> "1",
        "startDate" -> java.text.DateFormat.getDateInstance.format(startDate),
        "endDate" -> java.text.DateFormat.getDateInstance.format(endDate)
      ))

      mongoWrapper use { mongo =>
        mongo.insertManyIntoMongo(Seq(doc1, doc2)).unsafeRunSync()
        val httpApp = api.Endpoints.endpoints(mongo).orNotFound
        val request = Request[IO](method = Method.GET, uri = uri)
        val response = httpApp.run(request).unsafeRunSync()

        response.status shouldBe org.http4s.Status.Ok
        val responseBody = response.as[Json].unsafeRunSync()
        responseBody.asArray.map(_.length) shouldBe Some(2)

        IO.unit
      }
    }
  }
}
