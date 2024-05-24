package com.cimri

import cats.effect.{ExitCode, IO, IOApp}
import com.cimri.api.{Endpoints, Server}

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    MongoWrapper("price_changes").use(mongo => Server(12345, Endpoints.endpoints(mongo)).getOrCreate) >>
      IO(ExitCode.Success)
}
