package anotherschema

import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.requests.searches.SearchHit
import io.circe.Json
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets
import java.util.Base64

case class Edge[T](totalCount:Long, endCursor:Option[String], hasNextPage:Boolean, nodes:Seq[T])

object Edge {
  private val logger = LoggerFactory.getLogger(getClass)
  private val encoder = Base64.getEncoder
//
//  def cursorValue(forRecord:Json, forIndex:Int) = (forRecord \\ "sort").headOption match {
//    case Some(sort)=>
//      logger.debug(s"got Sort param for record: ${sort.noSpaces}")
//      //see https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html - only works if there is a `sort[]` field
//      encoder.encodeToString(sort.noSpaces.getBytes(StandardCharsets.UTF_8))
//    case None=>
//      //logger.debug(s"got no Sort param in record: ${forRecord.noSpaces}")
//      val jsonContent = Json.obj("sortType"->Json.fromString("index"),"index"->Json.fromInt(forIndex))
//      encoder.encodeToString(jsonContent.noSpaces.getBytes(StandardCharsets.UTF_8))
//  }

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
}