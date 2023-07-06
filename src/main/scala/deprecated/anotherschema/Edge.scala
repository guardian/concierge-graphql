package deprecated.anotherschema

import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.requests.searches.SearchHit
import io.circe.Json
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Try
import io.circe.syntax._
@deprecated("you should be using com.gu.contentapi.porter.graphql")
case class Edge[T:io.circe.Decoder](totalCount:Long, endCursor:Option[String], hasNextPage:Boolean, nodes:Seq[T]) {
  def map[V:io.circe.Decoder](mapper:(T)=>V) = Edge[V](totalCount, endCursor, hasNextPage, nodes.map(mapper))
}
@deprecated("you should be using com.gu.contentapi.porter.graphql")
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

  /**
   * Why the hell do we need to do this? It's because elastic4s expects a `Seq[Any]` as its cursor value,
   * which is not particularly useful to us :(
   * @param from
   * @return
   */
  def decodeToAny(from:Json):Any = {
    (from.asNumber, from.asString, from.asBoolean) match {
      case (Some(n), _, _)=>
        (n.toLong, n.toInt, n.toBigInt, n.toDouble) match {
          case (Some(long), _, _, _)=>
            long
          case (_, Some(int), _, _) =>
            int
          case (_, _, Some(bigint), _) =>
            bigint
          case (_, _, _, double)=>
            double
        }
      case (_, Some(s), _)=>
        s
      case (_, _, b)=>
        b
    }
  }

  def btos(from:Array[Byte]) = new String(from)

  def decodeCursor(cursor:Option[String]):Either[String, Seq[Any]] = cursor match {
    case Some(value)=>
      for {
        decoded <- Try { new String(decoder.decode(value)) }.toEither.left.map(_.getMessage)
        parsed <- io.circe.parser.parse(decoded).left.map(_.getMessage())
        result = parsed.asArray match {
          case Some(arr)=>
            arr.map(decodeToAny)
          case _=>
            Nil
        }
      } yield result
    case None=>
      Right(Nil)
  }
}