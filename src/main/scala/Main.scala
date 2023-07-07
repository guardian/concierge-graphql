import cats.effect._
import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.IpLiteralSyntax
import com.sksamuel.elastic4s.ElasticNodeEndpoint
import datastore.ElasticsearchRepo
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.server.Router
import org.http4s.ember.server._
import org.http4s.implicits._
import org.slf4j.LoggerFactory
import security.Security.limitByTier
import security.{InternalTier, UserTier}
import scala.concurrent.duration._

object Main extends IOApp {
  private val logger = LoggerFactory.getLogger(getClass)
  val documentRepo = new ElasticsearchRepo(ElasticNodeEndpoint("http","localhost",9200, None))
  val server = new GraphQLServer(documentRepo)

  val graphqlService = HttpRoutes.of[IO] {
    case OPTIONS -> Root / "query" =>
      IO(Response(
        Status.Ok,
        headers=Headers("Access-Control-Allow-Origin" -> "*", "Access-Control-Allow-Methods"->"POST, GET, OPTIONS", "Access-Control-Allow-Headers" -> s"Content-Type, ${security.KongHeader.name}")
      ))
    case req @ POST -> Root / "query" =>
      limitByTier(req, InternalTier) {
        server.handleRequest(req)
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
