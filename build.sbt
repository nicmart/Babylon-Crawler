import Dependencies._

val circeVersion = "0.7.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies ++= List(
        scalaTest % Test,
        "net.ruippeixotog" %% "scala-scraper" % "1.2.0",
        "com.typesafe.akka" %% "akka-actor" % "2.5.0",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion
    )
  )
