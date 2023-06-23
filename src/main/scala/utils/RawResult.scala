package utils

import com.sksamuel.elastic4s.Hit
import io.circe.{Json, ParsingFailure}

case class RawResult(score:Float, index:String, content: Json)

object RawResult {
  def apply(from:Hit):Either[ParsingFailure, RawResult] = {
    io.circe.parser.parse(from.sourceAsString)
      .map(json=>RawResult(
        from.score,
        from.index,
        json
      ))
  }
}