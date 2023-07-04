package com.gu.contentapi.porter.model

import java.time.LocalDateTime

case class Sponsorship(
  sponsorshipType: String,
  sponsorName: String,
  sponsorLogo: String,
  sponsorLogoDimensions: Option[SponsorshipLogoDimensions],
  highContrastSponsorLogo: Option[String],
  highContrastSponsorLogoDimensions: Option[SponsorshipLogoDimensions],
  sponsorLink: String,
  aboutLink: Option[String],
  targeting: Option[SponsorshipTargeting],
  validFrom: Option[LocalDateTime],
  validTo: Option[LocalDateTime])

case class SponsorshipLogoDimensions(width: Int, height: Int)

case class SponsorshipTargeting(
  publishedSince: Option[LocalDateTime],
  validEditions: Option[List[String]])
