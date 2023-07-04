package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class Pillar(id: String, name: String, sectionIds: Seq[String])

object Pillar {
  implicit val codecPillar: Codec[Pillar] = deriveCodec
}
