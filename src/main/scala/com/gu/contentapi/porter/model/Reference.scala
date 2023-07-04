package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class Reference(`type`: String, id: String)

object Reference {
  implicit val codecReference: Codec[Reference] = deriveCodec
}