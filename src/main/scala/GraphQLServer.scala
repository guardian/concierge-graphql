import cats.effect.IO
import org.http4s.{Request, Response, Status}
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer._
import fs2.Stream
import org.http4s.dsl.io._
import sangria.execution.Executor
import sangria.renderer.SchemaRenderer
import datastore.{DocumentRepo, GQLQueryContext}
import sangria.marshalling.circe._
import io.circe.syntax._
import middleware.{FieldMetrics, FieldPermissions}
import org.slf4j.LoggerFactory
import sangria.ast.Document
import security.UserTier
import utils.GraphQLRequestBody

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class GraphQLServer(documentRepo:DocumentRepo) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val inUseSchema = com.gu.contentapi.porter.graphql.RootQuery.schema

  private val metrics = FieldMetrics.singleton
  private val permissions = FieldPermissions.singleton

  private def parser(content:String) = Stream.apply(QueryParser.parse(content))

  private def performQuery(doc:Document, variables:Option[Map[String,String]], tier:UserTier) =
    IO.fromFuture(
      IO {
        val context = GQLQueryContext(documentRepo, tier)
        //FIXME - add in variables here
        Executor.execute(inUseSchema, doc, middleware = permissions :: metrics :: Nil, userContext = context).map(_.asJson.noSpaces)
      }
    )
    .map(body=>Ok(body))
    .handleError(err=>{
      logger.error(s"Could not run query: ${err.getMessage}", err)
        BadRequest(err.getMessage)
    })
      .flatten

  def handleRequest(req:Request[IO], tier:UserTier) = {
    for {
      bodyText <- req.bodyText
      gqlRequest <- Stream.apply(GraphQLRequestBody.parseJsonRequest(bodyText))
      parsedQuery <- parser(gqlRequest.query)
      result <- parsedQuery match {
        case Success(doc)=>
          Stream.apply(performQuery(doc, gqlRequest.variables, tier))
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
