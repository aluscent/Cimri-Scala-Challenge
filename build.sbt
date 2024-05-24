ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

val akkaVersion = "2.8.5"
val akkaHttpVersion = "10.5.3"
val http4sVersion = "0.23.12"

lazy val root = (project in file("."))
  .settings(
    name := "Cimri-Scala-Challenge",
    libraryDependencies ++= Seq(
      "org.mongodb.scala" %% "mongo-scala-driver" % "5.1.0",
      "org.slf4j" % "slf4j-simple" % "2.0.13",

      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.typelevel" %% "cats-effect" % "3.4.0",

      "io.spray" %% "spray-json" % "1.3.6",
      "io.circe" %% "circe-generic" % "0.14.7"
    )
  )
