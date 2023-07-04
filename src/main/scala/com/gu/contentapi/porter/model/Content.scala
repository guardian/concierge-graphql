package com.gu.contentapi.porter.model

import io.circe.generic.JsonCodec
import java.time.LocalDateTime

case class Content(
  id: String,
  `type`: String,
  alternateIds: Seq[String],
  contentAliases: Option[ContentAliases],
  webTitle: String,
  webPublicationDate: Option[LocalDateTime],
  sectionId: String,
  blocks: ContentBlocks,
  stats: ContentStats,
  fields: ContentFields,
  channels: Seq[ContentChannel],
  tags: Seq[String],
  leadTags: Seq[String],
  elements: Option[Set[ContentElement]],
  references: Seq[Reference],
  rights: Option[ContentRights],
  expiry: ContentExpiry,
  isExpired: Option[Boolean],
  isGone: Boolean,
  thumbnail: Option[Image],
  debug: DebugFields,
  atomIds: Option[Seq[AtomID]] = None,
  isHosted: Boolean)

@JsonCodec case class ContentAliasPath(
  path: String,
  ceasedToBeCanonicalAt: LocalDateTime)

@JsonCodec case class ContentAliases(
  firstPublishedPath: Option[String],
  canonicalPath: String,
  aliasPaths: Seq[ContentAliasPath])

@JsonCodec case class ContentFields(
  headline: Option[String] = None,
  byline: Option[String] = None,
  trailText: Option[String] = None,
  main: Option[String] = None,
  body: Option[String] = None,
  publication: Option[String] = None,
  productionOffice: Option[String] = None,
  contributorBio: Option[String] = None,
  wordcount: Option[Int] = None,
  shortSocialShareText: Option[String] = None,
  socialShareText: Option[String] = None,
  shortUrl: Option[String] = None,
  firstPublicationDate: Option[LocalDateTime] = None,
  scheduledPublicationDate: Option[LocalDateTime] = None,
  creationDate: Option[LocalDateTime] = None,
  lastModified: Option[LocalDateTime] = None,
  newspaperEditionDate: Option[LocalDateTime] = None,
  newspaperPageNumber: Option[Int] = None,
  hasStoryPackage: Option[Boolean] = None,
  allowUgc: Option[Boolean] = None,
  commentable: Option[Boolean] = None,
  commentCloseDate: Option[LocalDateTime] = None,
  isPremoderated: Option[Boolean] = None,
  isInappropriateForSponsorship: Option[Boolean] = None,
  showInRelatedContent: Option[Boolean] = None,
  legallySensitive: Option[Boolean] = None,
  sensitive: Option[Boolean] = None,
  showAffiliateLinks: Option[Boolean] = None,
  shouldHideAdverts: Option[Boolean] = None,
  shouldHideReaderRevenue: Option[Boolean] = None,
  liveBloggingNow: Option[Boolean] = None,
  membershipAccess: Option[String] = None,
  thumbnail: Option[String] = None,
  secureThumbnail: Option[String] = None,
  internalContentCode: Option[Long] = None,
  internalComposerCode: Option[String] = None,
  internalPageCode: Long,
  internalOctopusCode: Option[Long] = None,
  internalStoryPackageCode: Option[Long] = None,
  internalRevision: Option[Long] = None,
  internalShortId: Option[String] = None,
  standfirst: Option[String] = None,
  starRating: Option[Short] = None,
  displayHint: Option[String] = None,
  isPrintSent: Option[Boolean] = None,
  isLive: Boolean,
  lang: Option[String] = None,
  bodyText: Option[String] = None,
  charCount: Option[Int] = None,
  internalVideoCode: Option[String] = None,
  internalCommissionedWordcount: Option[Int] = None,
  bylineHtml: Option[String] = None,
  showTableOfContents: Option[Boolean] = None)

@JsonCodec case class ContentChannel(
  channelId: String,
  fields: ContentChannelFields)

@JsonCodec case class ContentChannelFields(
  isAvailable: Boolean,
  publicationDate: Option[LocalDateTime] = None)

@JsonCodec case class ContentElement(
  id: String,
  `type`: String,
  relation: String,
  assets: Set[ContentElementAsset])

@JsonCodec case class ContentElementAsset(
  file: String,
  mimeType: String,
  `type`: String,
  typeData: Map[String, String])

@JsonCodec case class Image(
  fields: ImageTypeData,
  assets: Seq[Asset])

@JsonCodec case class ContentRights(
  developerCommunity: Boolean,
  subscriptionDatabases: Boolean,
  syndicatable: Boolean)

@JsonCodec case class ContentExpiry(
  rights: ExpiryDetails,
  commercial: ExpiryDetails)

@JsonCodec case class ExpiryDetails(
  expired: Boolean,
  expiredAt: Option[LocalDateTime] = None,
  scheduledExpiry: Option[LocalDateTime] = None)

/**
 * The blocks that make up a piece of content.
 * @param main The main block, which will include the main image and other furniture
 * @param body The block(s) that make up the body of the content.
 *             For a liveblog there may be multiple blocks. Any other content will have only one block.
 */
@JsonCodec case class ContentBlocks(
  main: Option[Block],
  body: Option[Seq[Block]])

/**
 * A block of content.
 * @param id a unique ID
 * @param bodyHtml the HTML body of the block
 * @param bodyTextSummary the textual content of the block, with HTML tags stripped.
 *                        This will not include any non-textual content such as pullquotes, tweet embeds, etc.
 * @param title the block's title, if it has one
 * @param attributes metadata about the block
 *                   e.g. this will contain "keyEvent" -> "true" if the block is a key event,
 *                   or "summary" -> "true" if it is a summary
 * @param published whether this block is currently live
 * @param createdDate the first time this block was created
 * @param firstPublishedDate the first time this block was published
 * @param publishedDate the last time this block was published
 * @param lastModifiedDate the last time this block was modified
 * @param contributors people who contributed to this block
 * @param createdBy person who created this block
 * @param lastModifiedBy person who last modified this block
 */
@JsonCodec case class Block(
  id: String,
  bodyHtml: String,
  bodyTextSummary: String,
  title: Option[String] = None,
  attributes: BlockAttributes,
  published: Boolean,
  createdDate: Option[LocalDateTime] = None,
  firstPublishedDate: Option[LocalDateTime] = None,
  publishedDate: Option[LocalDateTime] = None,
  lastModifiedDate: Option[LocalDateTime] = None,
  contributors: Seq[String] = Nil,
  createdBy: Option[User] = None,
  lastModifiedBy: Option[User] = None,
  elements: Option[Seq[Element]] = None)

@JsonCodec case class BlockAttributes(
  keyEvent: Option[String] = None,
  summary: Option[String] = None,
  title: Option[String] = None,
  pinned: Option[String] = None,
  membershipPlaceholder: Option[MembershipPlaceholder] = None)

@JsonCodec case class MembershipPlaceholder(
  campaignCode: Option[String])

@JsonCodec case class ContentStats(
  videos: Int,
  images: Int,
  text: Int,
  tweets: Int,
  pullquotes: Int,
  audio: Int,
  interactives: Int,
  witness: Int,
  richlinks: Int,
  membership: Int,
  embeds: Int,
  comments: Int,
  instagram: Int,
  vines: Int,
  code: Int,
  callouts: Int)

@JsonCodec case class User(
  email: String,
  firstName: Option[String],
  lastName: Option[String])

@JsonCodec case class Element(
  `type`: String,
  assets: Option[Seq[Asset]],
  textTypeData: Option[TextTypeData] = None,
  videoTypeData: Option[VideoTypeData] = None,
  tweetTypeData: Option[TweetTypeData] = None,
  imageTypeData: Option[ImageTypeData] = None,
  pullquoteTypeData: Option[PullquoteTypeData] = None,
  audioTypeData: Option[AudioTypeData] = None,
  interactiveTypeData: Option[InteractiveTypeData] = None,
  mapTypeData: Option[StandardTypeData] = None,
  documentTypeData: Option[StandardTypeData] = None,
  tableTypeData: Option[StandardTypeData] = None,
  witnessTypeData: Option[WitnessTypeData] = None,
  richLinkTypeData: Option[RichLinkTypeData] = None,
  membershipTypeData: Option[MembershipTypeData] = None,
  embedTypeData: Option[EmbedTypeData] = None,
  commentTypeData: Option[CommentTypeData] = None,
  instagramTypeData: Option[InstagramTypeData] = None,
  vineTypeData: Option[VineTypeData] = None,
  contentAtomTypeData: Option[ContentAtomTypeData] = None,
  codeTypeData: Option[CodeTypeData] = None,
  calloutTypeData: Option[CalloutTypeData] = None)

@JsonCodec case class Asset(
  `type`: String,
  mimeType: String,
  file: String,
  typeData: AssetTypeData)

@JsonCodec case class TextTypeData(
  html: Option[String],
  role: Option[String])

@JsonCodec case class PullquoteTypeData(
  html: Option[String],
  attribution: Option[String],
  role: Option[String])

@JsonCodec case class AudioTypeData(
  html: Option[String],
  source: Option[String],
  description: Option[String],
  title: Option[String],
  credit: Option[String],
  caption: Option[String],
  authorName: Option[String],
  originalUrl: Option[String],
  height: Option[Int],
  width: Option[Int],
  durationMinutes: Option[Int],
  durationSeconds: Option[Int],
  explicit: Option[Boolean],
  clean: Option[Boolean],
  mediaId: Option[String],
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class TweetTypeData(
  source: Option[String],
  url: Option[String],
  id: Option[String],
  html: Option[String],
  originalUrl: Option[String],
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class VideoTypeData(
  url: Option[String],
  description: Option[String],
  title: Option[String],
  html: Option[String],
  source: Option[String],
  credit: Option[String],
  caption: Option[String],
  height: Option[Int],
  width: Option[Int],
  duration: Option[Int],
  contentAuthSystem: Option[String],
  embeddable: Option[String],
  isInappropriateForAdverts: Option[Boolean],
  mediaId: Option[String],
  thumbnailImageUrl: Option[String],
  shortUrl: Option[String],
  role: Option[String],
  originalUrl: Option[String],
  holdingImageSource: Option[String],
  holdingImagePhotographer: Option[String],
  holdingImagePicdarUrn: Option[String],
  holdingImageCopyright: Option[String],
  holdingImageSuppliersReference: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class ImageTypeData(
  caption: Option[String],
  copyright: Option[String],
  displayCredit: Option[Boolean],
  credit: Option[String],
  source: Option[String],
  photographer: Option[String],
  alt: Option[String],
  mediaId: Option[String],
  mediaApiUri: Option[String],
  picdarUrn: Option[String],
  suppliersReference: Option[String],
  imageType: Option[String],
  comment: Option[String],
  role: Option[String])

@JsonCodec case class InteractiveTypeData(
  url: Option[String],
  originalUrl: Option[String],
  source: Option[String],
  caption: Option[String],
  alt: Option[String],
  scriptUrl: Option[String],
  html: Option[String],
  scriptName: Option[String],
  iframeUrl: Option[String],
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class StandardTypeData(
  url: Option[String],
  originalUrl: Option[String],
  source: Option[String],
  title: Option[String],
  description: Option[String],
  credit: Option[String],
  caption: Option[String],
  width: Option[Int],
  height: Option[Int],
  html: Option[String],
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class WitnessTypeData(
  url: Option[String],
  originalUrl: Option[String],
  witnessEmbedType: Option[String],
  mediaId: Option[String],
  source: Option[String],
  title: Option[String],
  description: Option[String],
  authorName: Option[String],
  authorUsername: Option[String],
  authorWitnessProfileUrl: Option[String],
  authorGuardianProfileUrl: Option[String],
  caption: Option[String],
  alt: Option[String],
  width: Option[Int],
  height: Option[Int],
  html: Option[String],
  apiUrl: Option[String],
  photographer: Option[String],
  dateCreated: Option[LocalDateTime],
  youtubeUrl: Option[String],
  youtubeSource: Option[String],
  youtubeTitle: Option[String],
  youtubeDescription: Option[String],
  youtubeAuthorName: Option[String],
  youtubeHtml: Option[String],
  role: Option[String])

@JsonCodec case class RichLinkTypeData(
  url: Option[String],
  originalUrl: Option[String],
  linkText: Option[String],
  linkPrefix: Option[String],
  role: Option[String],
  sponsorship: Option[Sponsorship] = None)

@JsonCodec case class MembershipTypeData(
  originalUrl: Option[String],
  linkText: Option[String],
  linkPrefix: Option[String],
  title: Option[String],
  venue: Option[String],
  location: Option[String],
  identifier: Option[String],
  image: Option[String],
  price: Option[String],
  start: Option[LocalDateTime],
  end: Option[LocalDateTime],
  role: Option[String])

@JsonCodec case class EmbedTypeData(
  html: Option[String],
  safeEmbedCode: Option[Boolean],
  alt: Option[String],
  isMandatory: Option[Boolean],
  role: Option[String],
  caption: Option[String])

@JsonCodec case class CommentTypeData(
  source: Option[String],
  discussionKey: Option[String],
  commentUrl: Option[String],
  originalUrl: Option[String],
  sourceUrl: Option[String],
  discussionUrl: Option[String],
  authorUrl: Option[String],
  html: Option[String],
  authorName: Option[String],
  commentId: Option[Int],
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class AssetTypeData(
  aspectRatio: Option[String],
  altText: Option[String],
  isInappropriateForAdverts: Option[Boolean],
  caption: Option[String],
  credit: Option[String],
  embeddable: Option[Boolean],
  photographer: Option[String],
  source: Option[String],
  width: Option[Int],
  height: Option[Int],
  name: Option[String],
  secureFile: Option[String],
  isMaster: Option[Boolean],
  sizeInBytes: Option[Long])

@JsonCodec case class InstagramTypeData(
  originalUrl: String,
  title: String,
  source: String,
  authorUrl: String,
  authorUsername: String,
  html: Option[String],
  width: Option[Int],
  alt: Option[String],
  caption: Option[String],
  role: Option[String])

@JsonCodec case class VineTypeData(
  originalUrl: String,
  title: String,
  source: String,
  authorUrl: String,
  authorUsername: String,
  html: Option[String],
  width: Option[Int],
  height: Option[Int],
  alt: Option[String],
  caption: Option[String],
  role: Option[String])

@JsonCodec case class ContentAtomTypeData(
  atomId: String,
  atomType: String,
  role: Option[String],
  isMandatory: Option[Boolean])

@JsonCodec case class CodeTypeData(
  html: Option[String],
  language: Option[String])

@JsonCodec case class CalloutTypeData(
  campaignId: Option[String],
  isNonCollapsible: Option[Boolean],
  overridePrompt: Option[String],
  overrideTitle: Option[String],
  overrideDescription: Option[String])

@JsonCodec case class DebugFields(
  lastSeenByPorterAt: LocalDateTime,
  revisionSeenByPorter: Option[Long] = None,
  contentSource: Option[String] = None,
  originatingSystem: Option[String] = None)

sealed trait AtomID {
  val id: String
}
object AtomID {
  case class QuizID(id: String) extends AtomID
  case class MediaID(id: String) extends AtomID
  case class ExplainerID(id: String) extends AtomID
  case class CtaID(id: String) extends AtomID
  case class InteractiveID(id: String) extends AtomID
  case class RecipeID(id: String) extends AtomID
  case class ReviewID(id: String) extends AtomID
  case class QAndAID(id: String) extends AtomID
  case class GuideID(id: String) extends AtomID
  case class ProfileID(id: String) extends AtomID
  case class TimelineID(id: String) extends AtomID
  case class CommonsdivisionID(id: String) extends AtomID
  case class ChartID(id: String) extends AtomID
  case class AudioID(id: String) extends AtomID
  case class EmailsignupID(id: String) extends AtomID
}
