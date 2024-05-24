## Instructions for Setting Up and Running the Application

### Setup MongoDB:

Ensure MongoDB is pulled and running via Docker on your local machine or a remote server.
Update the uri, databaseName, and collectionName in MainAPI.scala/MainFileProcessor.scala as needed.

Project Structure:
```
your-project/
    ├── .gitignore
    ├── build.sbt
    ├── docker-compose.yml
    ├── readme.md
    ├── src/
        ├── main/
            ├── scala/
                ├── com/
                    ├── cimri/
                        ├── api/
                            ├── Endpoints.scala
                            └── Server.scala
                        ├── db/
                            └── MongoWrapper.scala
                        ├── fileprocessor/
                            └── FileHandler.scala
                        ├── implicits/
                            └── FeedImplicits.scala
                        ├── model/
                            ├── Feed.scala
                            └── FeedsFile.scala
                        ├── MainAPI.scala
                        └── MainFileProcessor.scala
```

### build.sbt:

Ensure your build.sbt includes the necessary dependencies:
```
ThisBuild / scalaVersion := "2.12.18"

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
```

### API Endpoints:

#### Query a document by its ID
Example: ```GET http://localhost:12345/api/getById/1```

#### Query documents by ID and date interval
Example: ```GET http://localhost:12345/api/getByIdAndDate?id=1&startDate=2022-01-01&endDate=2022-01-31```


This setup ensures that the MongoDB connection is properly managed, bulk insert operations are awaited, and the API is correctly defined and running.