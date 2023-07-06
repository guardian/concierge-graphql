package utils

import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.requests.searches.SearchHit
import deprecated.anotherschema.Edge
import io.circe.{Json, ParsingFailure}
import io.circe.generic.auto
import io.circe.syntax._

import java.nio.charset.StandardCharsets
import java.util.Base64
import scala.util.Try

case class RawResult(score:Float, index:String, content: Json, sort:Option[Seq[Any]]) {
  def fulljson:Json = {
    val fieldsToAdd = Seq(
      Json.fromFloat(score).map(jsScore => ("score" -> jsScore)),
      Edge.cursorValue(sort).map(cur=>("cursor" -> Json.fromString(cur)))
    ).collect({case Some(result)=>result})

    fieldsToAdd
      .foldLeft(content.asObject)((acc, elem)=>acc.map(_.+:(elem)))
      .asJson
  }
}

object RawResult {
  def apply(from:SearchHit):Either[ParsingFailure, RawResult] = {
    io.circe.parser.parse(from.sourceAsString)
      .map(json=>RawResult(
        from.score,
        from.index,
        json,
        from.sort
      ))
  }
}