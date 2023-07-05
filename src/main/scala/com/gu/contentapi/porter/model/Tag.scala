package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class Tag(
  id: String,
  alternateIds: List[String],
  sectionId: Option[String],
  webTitle: String,
  references: List[Reference],
  `type`: String,
  bio: Option[String],
  bylineImageUrl: Option[String],
  bylineLargeImageUrl: Option[String],
  description: Option[String],
  emailAddress: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  path: String,
  twitterHandle: Option[String],
  podcast: Option[TagPodcast],
  r2ContributorId: Option[String],
  rcsId: Option[String],
  paidContentType: Option[String],
  paidContentCampaignColour: Option[String],
  activeSponsorships: Option[List[Sponsorship]],
  expired: Boolean,
  campaignInformationType: Option[String],
  internalName: Option[String],
  tagCategories: Option[Set[String]] = None,
  entityIds: Option[Set[String]] = None)

case class TagPodcast(
  author: String,
  copyright: String,
  explicit: Boolean,
  linkUrl: String,
  subscriptionUrl: Option[String],
  image: Option[String],
  categories: List[PodcastCategory],
  podcastType: Option[String],
  googlePodcastsUrl: Option[String],
  spotifyUrl: Option[String],
  acastId: Option[String],
  pocketCastsUrl: Option[String])

case class PodcastCategory(
  main: String,
  sub: Option[String])

//object Tag {
//  implicit val codecPodcastCategory: Codec[PodcastCategory] = deriveCodec
//  implicit val codecTagPodcast: Codec[TagPodcast] = deriveCodec
//  implicit val codecTag: Codec[Tag] = deriveCodec
//}