import Dependencies._

val circeVersion = "0.7.0"
val akkaVersion = "2.5.0"

lazy val commonSettings = List(
    organization := "babylons.nhs",
    scalaVersion := "2.12.1",
    version      := "0.1.0-SNAPSHOT"
)

lazy val common = (project in file("nhs-common")).
    settings(
        inThisBuild(commonSettings),
        name := "NHS-Common"
    )

lazy val scraper = (project.dependsOn(common) in file("nhs-scraper")).
  settings(
    inThisBuild(commonSettings),
    name := "NHS-Scraper",
    libraryDependencies ++= List(
        scalaTest % Test,
        "net.ruippeixotog" %% "scala-scraper" % "1.2.0",
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion
    )
  )
