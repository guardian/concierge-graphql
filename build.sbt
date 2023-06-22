ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.11"

val elastic4sVersion = "7.17.4"

lazy val root = (project in file("."))
  .settings(
    name := "concierge-graphql",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % "0.23.21",
      "org.http4s" %% "http4s-dsl" % "0.23.21",
      "org.http4s" %% "http4s-ember-server" % "0.23.21",
      "org.sangria-graphql" %% "sangria" % "4.0.1",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.2",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",

      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
      "com.softwaremill.sttp.client3" %% "http4s-backend" % "3.8.15",

      "com.sksamuel.elastic4s" %% "elastic4s-client-sttp" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sVersion,
      // test kit
      "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"
    )
  )
