package utils

import io.circe.Json
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

case class GraphQLRequestBody(query:String, operationName:Option[String], variables:Option[Json])

object GraphQLRequestBody extends ((String, Option[String], Option[Json])=>GraphQLRequestBody){
  private val logger = LoggerFactory.getLogger(getClass)

  def parseJsonRequest(content:String):GraphQLRequestBody = {
    io.circe.parser.parse(content)
      .flatMap(_.as[GraphQLRequestBody]) match {
      case Left(err)=>
        logger.error(s"Invalid input response: ${err}")
        throw new RuntimeException("Invalid input json")  //TODO: put in a proper exception type or do error handling better!
      case Right(req)=>
        req
    }
  }
}