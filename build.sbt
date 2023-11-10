enablePlugins(DebianPlugin, JavaServerAppPackaging, SystemdPlugin, DockerPlugin, AshScriptPlugin)

ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.11"

val elastic4sVersion = "7.17.4"

maintainer := "Content Platforms team <content-platforms.dev@theguardian.com>"
packageSummary := "A proof-of-concept for doing CAPI queries with GraphQL"

val prometheusVersion = "0.16.0"

lazy val root = (project in file("."))
  .settings(
    name := "concierge-graphql",
    dockerBaseImage := "amazoncorretto:17-alpine",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-core" % "0.23.21",
      "org.http4s" %% "http4s-dsl" % "0.23.21",
      "org.http4s" %% "http4s-ember-server" % "0.23.21",
      "org.sangria-graphql" %% "sangria" % "4.0.1",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.2",
      "io.circe" %% "circe-core" % "0.14.5",
      "io.circe" %% "circe-parser" % "0.14.5",
      "io.circe" %% "circe-generic" % "0.14.5",
      "io.circe" %% "circe-optics" % "0.14.1",

      "ch.qos.logback" % "logback-classic" % "1.4.7",
      "net.logstash.logback" % "logstash-logback-encoder" % "7.3",

      "com.softwaremill.sttp.client3" %% "core" % "3.8.15",
      "com.softwaremill.sttp.client3" %% "http4s-backend" % "3.8.15",

      "com.sksamuel.elastic4s" %% "elastic4s-client-sttp" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sVersion,

      //metrics
      "io.prometheus" % "simpleclient" % prometheusVersion,
      "io.prometheus" % "simpleclient_hotspot" % prometheusVersion,
      "io.prometheus" % "simpleclient_common" % prometheusVersion,

      // test kit
      "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"
    ),
    dependencyOverrides ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.5",  //required for json logging encoder
      "io.netty" % "netty-handler" % "4.1.94.Final",
      "io.netty" % "netty-codec-http" % "4.1.94.Final",
    )
  )

