import cats.effect.IO
import org.http4s.{Request, Response, Status}
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer._
import fs2.Stream
import org.http4s.dsl.io._
import sangria.execution.Executor
import sangria.renderer.SchemaRenderer
import datastore.DocumentRepo
import sangria.marshalling.circe._
import io.circe.syntax._
import org.slf4j.LoggerFactory
import sangria.ast.Document
import utils.GraphQLRequestBody

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class GraphQLServer(documentRepo:DocumentRepo) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val inUseSchema = com.gu.contentapi.porter.graphql.RootQuery.schema

  private def parser(content:String) = Stream.apply(QueryParser.parse(content))

  def justATest:Future[String] = Future {
    Thread.sleep(500)
    "this is a test"
  }

  private def performQuery(doc:Document, variables:Option[Map[String,String]]) =
    IO.fromFuture(
      IO(
        Executor.execute(inUseSchema, doc, userContext = documentRepo).map(_.asJson.noSpaces)
      )
    )
    .map(body=>Ok(body))
    .handleError(err=>{
      logger.error(s"Could not run query: ${err.getMessage}", err)
        BadRequest(err.getMessage)
    })
      .flatten

  def handleRequest(req:Request[IO]) = {
    for {
      bodyText <- req.bodyText
      gqlRequest <- Stream.apply(GraphQLRequestBody.parseJsonRequest(bodyText))
      parsedQuery <- parser(gqlRequest.query)
      result <- parsedQuery match {
        case Success(doc)=>
          Stream.apply(performQuery(doc, gqlRequest.variables))
            .handleErrorWith(err => {
              logger.error("Error performing query", err)
              Stream.apply(InternalServerError(err.getMessage))
            })
        case Failure(err)=>
          println(s"Syntax error: ${err.getMessage}")
          Stream.apply(BadRequest(err.getMessage))
      }
    } yield result
  }

  def getSchema(schemaName:String) = schemaName match {
    case "content"=>
      Ok(SchemaRenderer.renderSchema(inUseSchema))
    case _=>
      NotFound("Schema not found")
  }
}
