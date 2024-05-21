ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.18"

val akkaVersion = "2.8.5"
val akkaHttpVersion = "10.5.3"

lazy val root = (project in file("."))
  .settings(
    name := "Cimri-Scala-Challenge",
    libraryDependencies ++= Seq(
      "org.mongodb.scala" %% "mongo-scala-driver" % "5.1.0",
      "org.slf4j" % "slf4j-simple" % "2.0.13",
      "io.spray" %% "spray-json" % "1.3.6",

      "com.typesafe.akka" %% "akka-actor" % akkaVersion,

      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
    )
  )
