ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

lazy val root = (project in file("."))
  .settings(
    name := "concierge-graphql",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % "0.23.21",
      "org.http4s" %% "http4s-dsl" % "0.23.21",
      "org.http4s" %% "http4s-ember-server" % "0.23.21",
      "org.sangria-graphql" %% "sangria" % "4.0.1",
      //"org.sangria-graphql" %% "sangria-circe" % "4.0.1",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
    )
  )
