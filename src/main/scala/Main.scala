import cats.effect._
import com.comcast.ip4s.{IpLiteralSyntax, Ipv4Address, Ipv6Address}
import datastore.ElasticsearchRepo
import io.prometheus.client.hotspot.DefaultExports
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.ember.server._
import org.http4s.implicits._
import org.slf4j.LoggerFactory
import security.{ApiKeyAuth, DeveloperTier, InternalTier, Security, UserTier}
import internalmetrics.PrometheusMetrics

import scala.concurrent.duration._
import utils.Config.fetchConfig

object Main extends IOApp {
  private val logger = LoggerFactory.getLogger(getClass)
  val config = fetchConfig().get  //it's OK to throw exception here, that will then block startup

  DefaultExports.initialize()
  val documentRepo = new ElasticsearchRepo(ElasticSearchResolver.resolve())
  val server = new GraphQLServer(documentRepo)

  private val security = Security(config)

  val graphqlService = HttpRoutes.of[IO] {
    case OPTIONS -> Root / "query" =>
      IO(Response(
        Status.Ok,
        headers=Headers("Access-Control-Allow-Origin" -> "*", "Access-Control-Allow-Methods"->"POST, GET, OPTIONS", "Access-Control-Allow-Headers" -> s"Content-Type, ${ApiKeyAuth.name.toString}")
      ))
    case req @ POST -> Root / "query" =>
      security.limitByTier(req, DeveloperTier) { tier=>
        server.handleRequest(req, tier)
          .compile
          .onlyOrError
          .flatten
          .map(response => response.copy(headers = response.headers.put("Content-Type" -> "application/json").put("Access-Control-Allow-Origin" -> "*")))
          .handleErrorWith(err=>{
            logger.error(s"Uncaught error when handling request: ${err.getMessage}", err)
            InternalServerError(err.getMessage())
          })
      }
    case GET -> Root / "healthcheck" =>
      Ok("Instance healthy")
    case GET -> Root / "schema" / name =>
      server
        .getSchema(name)
      .map(response => response.copy(headers = response.headers.put("Content-Type" -> "application/graphql")))
    case req @ GET -> Root / "metrics" =>
      val validAddresses = Seq(
        Ipv4Address.fromString("127.0.0.1").get,
        Ipv6Address.fromString("::1").get
      )

      if(req.remoteAddr.isDefined && validAddresses.contains(req.remoteAddr.get)) {
        Ok(PrometheusMetrics.dumpMetrics)
          .map(response => response.copy(headers = response.headers.put("Content-Type" -> "text/plain; version=0.0.4; charset=UTF-8")))
      } else {
        Forbidden("you do not have permission to access this endpoint")
      }
  }

  def run(args:List[String]):IO[ExitCode] = {
    val httpApp = Router("/" -> graphqlService).orNotFound
    logger.info("Starting up on 0.0.0.0 port 9000")
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"9000")
      .withIdleTimeout(2.seconds)
      .withShutdownTimeout(2.seconds)
      .withHttpApp(httpApp)
      .build
      .use(_=>IO.never)
      .as(ExitCode.Success)
  }
}
