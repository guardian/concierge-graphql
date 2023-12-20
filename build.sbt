enablePlugins(DebianPlugin, JavaServerAppPackaging, SystemdPlugin, DockerPlugin, AshScriptPlugin)
import scala.sys.process._
import com.typesafe.sbt.packager.docker
ThisBuild / version := "0.1.0"

ThisBuild / scalaVersion := "2.13.11"

val elastic4sVersion = "7.17.4"

maintainer := "Content Platforms team <content-platforms.dev@theguardian.com>"
packageSummary := "A proof-of-concept for doing CAPI queries with GraphQL"

val prometheusVersion = "0.16.0"

lazy val ensureDockerBuildx = taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx = taskKey[Unit]("Build docker images using buildx")
lazy val dockerBuildxSettings = Seq(
  ensureDockerBuildx := {
    if (Process("docker buildx inspect multi-arch-builder").! == 1) {
      Process("docker buildx create --use --name multi-arch-builder", baseDirectory.value).!
    }
  },
  dockerBuildWithBuildx := {
    streams.value.log("Building and pushing image with Buildx")
    dockerAliases.value.foreach(
      alias => Process("docker buildx build --platform=linux/arm64,linux/amd64 --push -t " +
        alias + " .", baseDirectory.value / "target" / "docker"/ "stage").!
    )
  },
  publish in Docker := Def.sequential(
    publishLocal in Docker,
    ensureDockerBuildx,
    dockerBuildWithBuildx
  ).value
)

lazy val root = (project in file("."))
  .settings(
    dockerBuildxSettings,
    name := "concierge-graphql",
    dockerBaseImage := "amazoncorretto:17-alpine",
    dockerRepository := Some("ghcr.io/guardian/concierge-graphql"),
    dockerAliases := Seq(docker.DockerAlias(registryHost=dockerRepository.value, username=None, name="concierge-graphql", tag=Some(sys.env.getOrElse("BUILD_NUMBER", "DEV")))),
    dockerExposedPorts := Seq(9000),
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

      "ch.qos.logback" % "logback-classic" % "1.4.12",
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

