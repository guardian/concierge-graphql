import cats.effect.IO
import org.http4s.{Request, Response, Status}
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer._
import fs2.Stream
import org.http4s.dsl.io._
import sangria.execution.Executor
import sangria.renderer.SchemaRenderer
import anotherschema.Content
import datastore.DocumentRepo
import sangria.marshalling.circe._
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class GraphQLServer(documentRepo:DocumentRepo) {
  private def parser(content:String) = Stream.apply(QueryParser.parse(content))

  def handleRequest(req:Request[IO]) = {
    for {
      bodyText <- req.bodyText
      parsedQuery <- parser(bodyText)
      result <- parsedQuery match {
        case Success(doc)=>
          println(renderPretty(doc))
          //Stream.apply(Ok(renderPretty(doc)))
          Stream.apply(
            IO.fromFuture(
              IO(
                Executor.execute(Content.ContentSchema, doc,
                  userContext = documentRepo,
                ).map(_.asJson.noSpaces)
              )
            )
          ).map(result=>Ok(result))
        case Failure(err)=>
          println(s"Syntax error: ${err.getMessage}")
          Stream.apply(BadRequest(err.getMessage))
      }
    } yield result
  }

  def getSchema(schemaName:String) = schemaName match {
    case "content"=>
      Ok(SchemaRenderer.renderSchema(Content.ContentSchema))
    case _=>
      NotFound("Schema not found")
  }
}
