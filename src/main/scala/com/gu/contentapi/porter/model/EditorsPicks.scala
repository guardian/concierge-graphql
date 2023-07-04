package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class EditorsPick(
  id: String,
  shortUrl: String,
  trailText: Option[String],
  headline: String,
  thumbnail: Option[String],
  group: Option[String],
  frontPublicationDate: Option[Long])

case class EditorsPicks(id: String, content: List[EditorsPick])

object EditorsPicks {
  implicit val codecEditorsPick: Codec[EditorsPick] = deriveCodec
  implicit val codecEditorsPicks: Codec[EditorsPicks] = deriveCodec
}