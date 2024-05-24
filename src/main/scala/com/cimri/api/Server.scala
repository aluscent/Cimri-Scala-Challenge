package com.cimri.api

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global
import scala.language.postfixOps

class Server(port: Int, api: HttpRoutes[IO]) {

  private val server = BlazeServerBuilder[IO]
    .withExecutionContext(global)
    .bindHttp(port, "localhost")
    .withHttpApp(api.orNotFound)
    .serve
    .compile
    .drain

  def getOrCreate: IO[Unit] = server
}

object Server {
  def apply(port: Int, api: HttpRoutes[IO]) = new Server(port, api)
}
