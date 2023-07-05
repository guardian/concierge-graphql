package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class Section(
  id: String,
  alternateIds: List[String],
  webTitle: String,
  localisationOf: Option[String],
  isLocalisation: Boolean,
  isMultiEdition: Boolean,
  microsite: Option[Boolean],
  editions: List[SectionEdition],
  discriminator: Option[String],
  edition: Option[String],
  placeholder: Option[Boolean],
  activeSponsorships: Option[List[Sponsorship]])

case class SectionEdition(
  id: String,
  path: String,
  webTitle: String,
  code: String)

//object Section {
//  implicit val codecSectionEdition: Codec[SectionEdition] = deriveCodec
//  implicit val codecSection: Codec[Section] = deriveCodec
//}
