import Dependencies._

val circeVersion = "0.7.0"
val akkaVersion = "2.5.0"
val http4sVersion = "0.15.8"
val luceneVersion = "6.5.0"

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

lazy val scraper = (project in file("nhs-scraper")).dependsOn(common).
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

lazy val http = (project in file ("nhs-http")).dependsOn(common).
    settings(
        inThisBuild(commonSettings),
        name := "NHS-Scraper",
        libraryDependencies ++= List(
            scalaTest % Test,
            "org.http4s"     %% "http4s-blaze-server" % http4sVersion,
            "org.http4s"     %% "http4s-circe"        % http4sVersion,
            "org.http4s"     %% "http4s-dsl"          % http4sVersion,
            "ch.qos.logback" %  "logback-classic"     % "1.2.1",
            "io.circe" %% "circe-core" % circeVersion,
            "io.circe" %% "circe-generic" % circeVersion,
            "io.circe" %% "circe-parser" % circeVersion,
            "org.apache.lucene" % "lucene-facet" % luceneVersion,
            "org.apache.lucene" % "lucene-analyzers-common" % luceneVersion,
            "org.apache.lucene" % "lucene-queryparser" % luceneVersion,
            "org.apache.lucene" % "lucene-expressions" % luceneVersion,
            "org.apache.lucene" % "lucene-spatial" % luceneVersion
        )
    )