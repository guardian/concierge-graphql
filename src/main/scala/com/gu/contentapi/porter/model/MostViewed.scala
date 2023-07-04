package com.gu.contentapi.porter.model

import io.circe._
import io.circe.generic.semiauto._

case class MostViewed(id: String, links: List[String])

object MostViewed {
  val decoderMostViewed: Decoder[MostViewed] = Decoder.instance { cursor =>
    for {
      id <- cursor.downField("id").as[String]
      links <- cursor.downField("content").as[List[String]]
      // ^ this is not a mistake, the links are under a property named `content`
    } yield MostViewed(id, links)
  }

  val encoderMostViewed: Encoder[MostViewed] = deriveEncoder

  implicit val codecMostViewed: Codec[MostViewed] = Codec.from(decoderMostViewed, encoderMostViewed)
}
