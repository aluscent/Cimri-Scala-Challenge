package com.cimri.api

import cats.effect.IO
import com.cimri.MongoWrapper
import io.circe.Encoder.AsObject.importedAsObjectEncoder
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.io._

import java.text.SimpleDateFormat

object Endpoints {
  def endpoints(mongoWrapper: MongoWrapper): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "api" / "getById" / id =>
      mongoWrapper.queryById(id).flatMap {
        case Some(value) => Ok(value.toJson)
        case None => NotFound()
      }
    case req@GET -> Root / "api" / "getByIdAndDate" =>
      (req.params.get("id"), req.params.get("startDate"), req.params.get("endDate")) match {
        case (Some(id), Some(startDate), Some(endDate)) =>
          mongoWrapper.queryByIdAndDate(
            id,
            new SimpleDateFormat("yyyy-MM-dd").parse(startDate),
            new SimpleDateFormat("yyyy-MM-dd").parse(endDate)
          ).flatMap {
            case list if list.nonEmpty => Ok(list.map(_.toJson))
            case _ => NotFound()
          }
        case _ => BadRequest("One or more parameters are missing.")
      }
    case _ => BadRequest("This request can't be processed.")
  }
}
