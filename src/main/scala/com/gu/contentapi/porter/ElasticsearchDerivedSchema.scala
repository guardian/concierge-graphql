package com.gu.contentapi.porter

import sangria.macros.derive._
import sangria.schema._
import java.time.format.DateTimeFormatter
import io.circe.generic.auto._
import io.circe.syntax._

object ElasticsearchDerivedSchema {
  //here's a thought - wouldn't it be cool if we could specify a timezone in the query, have that timezone passed down in context, and output the data time in the requested TZ?
  implicit val ContentAliasPath = deriveObjectType[Unit, model.ContentAliasPath](
    ReplaceField("ceasedToBeCanonicalAt", Field("ceasedToBeCanonicalAt", StringType, resolve = _.value.ceasedToBeCanonicalAt.format(DateTimeFormatter.ISO_DATE_TIME)))
  )
  implicit val ContentAliases = deriveObjectType[Unit, model.ContentAliases]()
  implicit val ContentFields = deriveObjectType[Unit, model.ContentFields](
    ReplaceField("firstPublicationDate", Field("firstPublicationDate", OptionType(StringType), resolve = _.value.firstPublicationDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("scheduledPublicationDate", Field("scheduledPublicationDate", OptionType(StringType), resolve = _.value.scheduledPublicationDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("creationDate", Field("creationDate", OptionType(StringType), resolve = _.value.creationDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("lastModified", Field("lastModified", OptionType(StringType), resolve = _.value.lastModified.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("newspaperEditionDate", Field("newspaperEditionDate", OptionType(StringType), resolve = _.value.newspaperEditionDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("commentCloseDate", Field("commentCloseDate", OptionType(StringType), resolve = _.value.commentCloseDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("starRating", Field("starRating", OptionType(IntType), resolve=_.value.starRating.map(_.toInt)))
  )
  implicit val ContentChannelFields = deriveObjectType[Unit, model.ContentChannelFields](
    ReplaceField("publicationDate", Field("publicationDate", OptionType(StringType), resolve = _.value.publicationDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME))))
  )
  implicit val ContentChannel = deriveObjectType[Unit, model.ContentChannel]()
  implicit val ContentElementAsset = deriveObjectType[Unit, model.ContentElementAsset](
    ReplaceField("typeData", Field("typeData", StringType, resolve=_.value.typeData.asJson.noSpaces))
  )
  implicit val ContentElement = deriveObjectType[Unit, model.ContentElement](
    ReplaceField("assets", Field("assets", ListType(ContentElementAsset), resolve=_.value.assets.toSeq))
  )

  implicit val ContentRights = deriveObjectType[Unit, model.ContentRights]()
  implicit val ExpiryDetails = deriveObjectType[Unit, model.ExpiryDetails](
    ReplaceField("expiredAt", Field("expiredAt", OptionType(StringType), resolve = _.value.expiredAt.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("scheduledExpiry", Field("scheduledExpiry", OptionType(StringType), resolve = _.value.scheduledExpiry.map(_.format(DateTimeFormatter.ISO_DATE_TIME))))
  )
  implicit val ContentExpiry = deriveObjectType[Unit, model.ContentExpiry]()

  implicit val ContentStats = deriveObjectType[Unit, model.ContentStats]()
  implicit val SponsorshipLogoDimensions = deriveObjectType[Unit, model.SponsorshipLogoDimensions]()
  implicit val SponsorshipTargeting = deriveObjectType[Unit, model.SponsorshipTargeting](
    ReplaceField("publishedSince", Field("publishedSince", OptionType(StringType), resolve = _.value.publishedSince.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
  )
  implicit val Sponsorshup = deriveObjectType[Unit, model.Sponsorship](
    ReplaceField("validFrom", Field("validFrom", OptionType(StringType), resolve = _.value.validFrom.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
    ReplaceField("validTo", Field("validTo", OptionType(StringType), resolve = _.value.validFrom.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))),
  )
  implicit val RichLinkTypeData = deriveObjectType[Unit, model.RichLinkTypeData]()
  implicit val MembershipTypeData = deriveObjectType[Unit, model.MembershipTypeData]()
  implicit val EmbedTypeData = deriveObjectType[Unit, model.EmbedTypeData]()
  implicit val CommentTypeData = deriveObjectType[Unit, model.CommentTypeData]()
  implicit val InstagramTypeData = deriveObjectType[Unit, model.InstagramTypeData]()
  implicit val VineTypeData = deriveObjectType[Unit, model.VineTypeData]()
  implicit val ContentAtomTypeData = deriveObjectType[Unit, model.ContentAtomTypeData]()
  implicit val CodeTypeData = deriveObjectType[Unit, model.CodeTypeData]()
  implicit val CalloutTypeData = deriveObjectType[Unit, model.CalloutTypeData]()
  implicit val DebugFields = deriveObjectType[Unit, model.DebugFields]()
  implicit val WitnessTypeData = deriveObjectType[Unit, model.WitnessTypeData](

  )
  implicit val StandardTypeData = deriveObjectType[Unit, model.StandardTypeData]()
  implicit val InteractiveTypeData = deriveObjectType[Unit, model.InteractiveTypeData]()
  implicit val AudioTypeData = deriveObjectType[Unit, model.AudioTypeData]()
  implicit val PullquoteTypeData = deriveObjectType[Unit, model.PullquoteTypeData]()
  implicit val ImageTypeData = deriveObjectType[Unit, model.ImageTypeData]()
  implicit val TweetTypeData = deriveObjectType[Unit, model.TweetTypeData]()
  implicit val VideoTypeData = deriveObjectType[Unit, model.VideoTypeData]()
  implicit val TextTypeData = deriveObjectType[Unit, model.TextTypeData]()
  implicit val AssetTypeData = deriveObjectType[Unit, model.AssetTypeData]()
  implicit val Image = deriveObjectType[Unit, model.Image]()
  implicit val Asset = deriveObjectType[Unit, model.Asset]()
  implicit val Element = deriveObjectType[Unit, model.Element]()
  implicit val User = deriveObjectType[Unit, model.User]()
  implicit val MembershipPlaceholder = deriveObjectType[Unit, model.MembershipPlaceholder]()
  implicit val BlockAttributes = deriveObjectType[Unit, model.BlockAttributes]()
  implicit val Block = deriveObjectType[Unit, model.Block](
    ReplaceField("createdDate",
      Field("createdDate", OptionType(StringType), resolve = _.value.createdDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))
    ),
      ReplaceField("firstPublishedDate",
      Field("firstPublishedDate", OptionType(StringType), resolve = _.value.firstPublishedDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))
    ),
      ReplaceField("publishedDate",
      Field("publishedDate", OptionType(StringType), resolve = _.value.publishedDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))
    ),
      ReplaceField("lastModifiedDate",
      Field("lastModifiedDate", OptionType(StringType), resolve = _.value.lastModifiedDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))
    ),

  )

  implicit val ContentBlocks = deriveObjectType[Unit, model.ContentBlocks]()
  implicit val Aliases = deriveObjectType[Unit, model.ContentAliases]()
  val Content = deriveObjectType[Unit, model.Content](
    ReplaceField("webPublicationDate",
      Field("webPublicationDate", OptionType(StringType), resolve = _.value.webPublicationDate.map(_.format(DateTimeFormatter.ISO_DATE_TIME)))
    )

  )

  val schema = Schema(Content)
}
