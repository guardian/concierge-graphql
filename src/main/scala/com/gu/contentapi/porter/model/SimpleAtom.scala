package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class SimpleAtom(id: String, atomType: String)

object SimpleAtom {
  implicit val codecSimpleAtom: Codec[SimpleAtom] = deriveCodec
}