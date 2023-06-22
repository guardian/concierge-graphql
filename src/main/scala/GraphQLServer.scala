import cats.effect.IO
import org.http4s.{Request, Response, Status}
import sangria.parser.QueryParser
import sangria.renderer.QueryRenderer._
import fs2.Stream
import org.http4s.dsl.io._
import sangria.renderer.SchemaRenderer
import schema.Content

import scala.util.{Failure, Success}

object GraphQLServer {
  private def parser(content:String) = Stream.apply(QueryParser.parse(content))

  def handleRequest(req:Request[IO]) = {
    for {
      bodyText <- req.bodyText
      parsedQuery <- parser(bodyText)
      result <- parsedQuery match {
        case Success(doc)=>
          println(renderPretty(doc))
          Stream.apply(Ok(renderPretty(doc)))
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
