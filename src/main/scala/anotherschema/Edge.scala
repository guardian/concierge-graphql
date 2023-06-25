package anotherschema

import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.requests.searches.SearchHit
import io.circe.Json
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Try

case class Edge[T](totalCount:Long, endCursor:Option[String], hasNextPage:Boolean, nodes:Seq[T])

object Edge {
  private val logger = LoggerFactory.getLogger(getClass)
  private val encoder = Base64.getEncoder
  private val decoder = Base64.getDecoder

  def cursorValue(forRecord:SearchHit):Option[String] = {
    cursorValue(forRecord.sort)
  }

  def cursorValue(forSort:Option[Seq[Any]]):Option[String] = {
    //Note - it's intentional that this will throw if we find a value we don't recognise
    for {
      json <- forSort.map(v => Json.arr(v.map({
        case strValue: String =>
          Json.fromString(strValue)
        case intValue: Integer =>
          Json.fromInt(intValue)
        case longValue: Long =>
          Json.fromLong(longValue)
        case floatValue: Float =>
          Json.fromFloat(floatValue).get
        case doubleValue: Double =>
          Json.fromDouble(doubleValue).get
      }): _*))
      encoded = encoder.encodeToString(json.noSpaces.getBytes(StandardCharsets.UTF_8))
    } yield encoded
  }

  def decodeCursor(cursor:Option[String]):Seq[Any] = cursor match {
    case Some(value)=>
      for {
        decoded <- Try { decoder.decode(value) }.toEither.left.map(_.getMessage)
        parsed <- io.circe.parser.parse( new String(decoded)).left.map(_.getMessage())
        result <- if(parsed.isArray) {
          parsed.
        } else Nil
      } yield parsed.
  }
}