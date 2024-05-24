package com.cimri.api

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global
import scala.language.postfixOps

/**
 * Creates a BlazeServer to listen to connections.
 * @param port listening port
 * @param api routes' endpoints
 */
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

  /**
   * Creates a BlazeServer for connections.
   * @param port listening port
   * @param api routes' endpoints
   */
  def apply(port: Int, api: HttpRoutes[IO]) = new Server(port, api)
}
