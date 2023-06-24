package utils

import com.sksamuel.elastic4s.Hit
import com.sksamuel.elastic4s.requests.searches.SearchHit
import io.circe.{Json, ParsingFailure}

case class RawResult(score:Float, index:String, content: Json, sort:Option[Seq[AnyRef]])

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