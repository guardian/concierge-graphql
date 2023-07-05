package com.gu.contentapi.porter.graphql

import sangria.macros.derive._
import sangria.schema._

import java.time.format.DateTimeFormatter
import io.circe.generic.auto._
import io.circe.syntax._
import com.gu.contentapi.porter.model
import datastore.DocumentRepo

object Content {
  //here's a thought - wouldn't it be cool if we could specify a timezone in the query, have that timezone passed down in context, and output the data time in the requested TZ?
  implicit val ContentAliasPath = deriveObjectType[Unit, model.ContentAliasPath](
    ReplaceField("ceasedToBeCanonicalAt", Field("ceasedToBeCanonicalAt", StringType, resolve = _.value.ceasedToBeCanonicalAt.format(DateTime.Formatter)))
  )
  implicit val ContentAliases = deriveObjectType[Unit, model.ContentAliases]()
  implicit val ContentFields = deriveObjectType[Unit, model.ContentFields](
    ReplaceField("firstPublicationDate", Field("firstPublicationDate", OptionType(StringType), resolve = _.value.firstPublicationDate.map(_.format(DateTime.Formatter)))),
    ReplaceField("scheduledPublicationDate", Field("scheduledPublicationDate", OptionType(StringType), resolve = _.value.scheduledPublicationDate.map(_.format(DateTime.Formatter)))),
    ReplaceField("creationDate", Field("creationDate", OptionType(StringType), resolve = _.value.creationDate.map(_.format(DateTime.Formatter)))),
    ReplaceField("lastModified", Field("lastModified", OptionType(StringType), resolve = _.value.lastModified.map(_.format(DateTime.Formatter)))),
    ReplaceField("newspaperEditionDate", Field("newspaperEditionDate", OptionType(StringType), resolve = _.value.newspaperEditionDate.map(_.format(DateTime.Formatter)))),
    ReplaceField("commentCloseDate", Field("commentCloseDate", OptionType(StringType), resolve = _.value.commentCloseDate.map(_.format(DateTime.Formatter)))),
    ReplaceField("starRating", Field("starRating", OptionType(IntType), resolve=_.value.starRating.map(_.toInt)))
  )
  implicit val ContentChannelFields = deriveObjectType[Unit, model.ContentChannelFields](
    ReplaceField("publicationDate", Field("publicationDate", OptionType(StringType), resolve = _.value.publicationDate.map(_.format(DateTime.Formatter))))
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
    ReplaceField("expiredAt", Field("expiredAt", OptionType(StringType), resolve = _.value.expiredAt.map(_.format(DateTime.Formatter)))),
    ReplaceField("scheduledExpiry", Field("scheduledExpiry", OptionType(StringType), resolve = _.value.scheduledExpiry.map(_.format(DateTime.Formatter))))
  )
  implicit val ContentExpiry = deriveObjectType[Unit, model.ContentExpiry]()

  implicit val ContentStats = deriveObjectType[Unit, model.ContentStats]()
  implicit val SponsorshipLogoDimensions = deriveObjectType[Unit, model.SponsorshipLogoDimensions]()
  implicit val SponsorshipTargeting = deriveObjectType[Unit, model.SponsorshipTargeting](
    ReplaceField("publishedSince", Field("publishedSince", OptionType(StringType), resolve = _.value.publishedSince.map(_.format(DateTime.Formatter)))),
  )
  implicit val Sponsorshup = deriveObjectType[Unit, model.Sponsorship](
    ReplaceField("validFrom", Field("validFrom", OptionType(StringType), resolve = _.value.validFrom.map(_.format(DateTime.Formatter)))),
    ReplaceField("validTo", Field("validTo", OptionType(StringType), resolve = _.value.validFrom.map(_.format(DateTime.Formatter)))),
  )
  implicit val RichLinkTypeData = deriveObjectType[Unit, model.RichLinkTypeData]()
  implicit val MembershipTypeData = deriveObjectType[Unit, model.MembershipTypeData](
    ReplaceField("start", Field("start", OptionType(StringType), resolve = _.value.start.map(_.format(DateTime.Formatter)))),
    ReplaceField("end", Field("end", OptionType(StringType), resolve = _.value.end.map(_.format(DateTime.Formatter)))),
  )
  implicit val EmbedTypeData = deriveObjectType[Unit, model.EmbedTypeData]()
  implicit val CommentTypeData = deriveObjectType[Unit, model.CommentTypeData]()
  implicit val InstagramTypeData = deriveObjectType[Unit, model.InstagramTypeData]()
  implicit val VineTypeData = deriveObjectType[Unit, model.VineTypeData]()
  implicit val ContentAtomTypeData = deriveObjectType[Unit, model.ContentAtomTypeData]()
  implicit val CodeTypeData = deriveObjectType[Unit, model.CodeTypeData]()
  implicit val CalloutTypeData = deriveObjectType[Unit, model.CalloutTypeData]()
  implicit val DebugFields = deriveObjectType[Unit, model.DebugFields](
    ReplaceField("lastSeenByPorterAt", Field("lastSeenByPorterAt", StringType, resolve = _.value.lastSeenByPorterAt.format(DateTime.Formatter)))
  )
  implicit val WitnessTypeData = deriveObjectType[Unit, model.WitnessTypeData](
    ReplaceField("dateCreated", Field("dateCreated", OptionType(StringType), resolve = _.value.dateCreated.map(_.format(DateTime.Formatter))))
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
  implicit val Asset = deriveObjectType[Unit, model.Asset]()
  implicit val Image = deriveObjectType[Unit, model.Image]()
  implicit val Element = deriveObjectType[Unit, model.Element]()
  implicit val User = deriveObjectType[Unit, model.User]()
  implicit val MembershipPlaceholder = deriveObjectType[Unit, model.MembershipPlaceholder]()
  implicit val BlockAttributes = deriveObjectType[Unit, model.BlockAttributes]()
  implicit val Block = deriveObjectType[Unit, model.Block](
    ReplaceField("createdDate",
      Field("createdDate", OptionType(StringType), resolve = _.value.createdDate.map(_.format(DateTime.Formatter)))
    ),
    ReplaceField("firstPublishedDate",
      Field("firstPublishedDate", OptionType(StringType), resolve = _.value.firstPublishedDate.map(_.format(DateTime.Formatter)))
    ),
    ReplaceField("publishedDate",
      Field("publishedDate", OptionType(StringType), resolve = _.value.publishedDate.map(_.format(DateTime.Formatter)))
    ),
    ReplaceField("lastModifiedDate",
      Field("lastModifiedDate", OptionType(StringType), resolve = _.value.lastModifiedDate.map(_.format(DateTime.Formatter)))
    ),

  )

  implicit val ContentBlocks = deriveObjectType[Unit, model.ContentBlocks]()
  implicit val Reference = deriveObjectType[Unit, model.Reference]()

  val Content = deriveObjectType[DocumentRepo, model.Content](
    ReplaceField("webPublicationDate",
      Field("webPublicationDate", OptionType(StringType), resolve = _.value.webPublicationDate.map(_.format(DateTime.Formatter)))
    ),
    ReplaceField("alternateIds", Field("alternateIds", ListType(StringType), arguments = AlternateIdParameters.AllAlternateIdParameters, resolve= AlternateIdParameters.Resolver[DocumentRepo])),
    ReplaceField("elements", Field("elements", OptionType(ListType(ContentElement)),resolve=_.value.elements.map(_.toSeq))),
    ReplaceField("atomIds", Field("atomIds", OptionType(ListType(StringType)), resolve=_.value.atomIds.map(_.map(_.id)))),
    ReplaceField("tags", Field("tags", OptionType(ListType(Tags.Tag)),
      arguments = TagQueryParameters.NonPaginatedTagQueryParameters,
      resolve=ctx=> ctx.ctx.tagsForList(ctx.value.tags, ctx arg TagQueryParameters.Section, ctx arg TagQueryParameters.TagType))
    )
  )

}
