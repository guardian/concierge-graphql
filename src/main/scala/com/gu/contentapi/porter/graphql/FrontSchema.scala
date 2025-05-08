package com.gu.contentapi.porter.graphql

import com.gu.contentapi.porter.graphql.RootQuery.articlesByPageCode
import datastore.GQLQueryContext
import sangria.schema._
import com.gu.facia.api.models.{Collection => FaciaCollection, Front => FaciaFront}
import com.gu.facia.client.models.{ImageSourceAsset, SlideshowAsset, SupportingItem, SupportingItemMetaData, Trail, TrailMetaData}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive.{AddFields, ReplaceField, deriveObjectType}

object FrontSchema {
  val description = "A front is a collection of containers that together represent a single page of curated content"

  val Front: ObjectType[GQLQueryContext, FaciaFront] = ObjectType[GQLQueryContext, FaciaFront](
    "Front",
    description,
    () => fields[GQLQueryContext, FaciaFront](
      Field(
        name = "id",
        fieldType = StringType,
        description = Some("The ID of the Front"),
        resolve = ctx => ctx.value.id
      ),
      Field(
        name = "collectionIds",
        fieldType = ListType(StringType),
        description = Some("An ordered list of ids of the Front's collection"),
        resolve = ctx => ctx.value.collections
      ),
      Field(
        name = "webTitle",
        fieldType = OptionType(StringType),
        description = Some("The title of the front as it appears on the web"),
        resolve = ctx => ctx.value.webTitle
      ),
      Field(
        name = "collections",
        fieldType = ListType(FrontSchema.Collection),
        description = Some("An ordered list of the Front's collections"),
        resolve = ctx => collectionFetcher.deferSeqOpt(ctx.value.collections)
      )
    )
  )

  implicit val reviewId: HasId[FaciaCollection, String] = _.id
  val collectionFetcher: Fetcher[GQLQueryContext, FaciaCollection, FaciaCollection, String] = Fetcher[GQLQueryContext, FaciaCollection, String] { (ctx: GQLQueryContext, ids: Seq[String]) =>
    ctx.frontsRepo.getCollections(ids)
  }
  private val TrailMetadataType = ObjectType[GQLQueryContext, TrailMetaData](
    "meta",
    "The metadata associated with a Trail",
    () => fields[GQLQueryContext, TrailMetaData](
      Field(name = "headline", fieldType = OptionType(StringType), description = Some(""), resolve = _.value.json.get("headline").flatMap(_.asOpt[String])),
      Field(name = "href",fieldType = OptionType(StringType), resolve = _.value.json.get("href").flatMap(_.asOpt[String])),
      Field(name = "snapType",fieldType = OptionType(StringType), resolve = _.value.json.get("snapType").flatMap(_.asOpt[String])),
      Field(name = "snapCss",fieldType = OptionType(StringType), resolve = _.value.json.get("snapCss").flatMap(_.asOpt[String])),
      Field(name = "snapUri",fieldType = OptionType(StringType), resolve = _.value.json.get("snapUri").flatMap(_.asOpt[String])),
      Field(name = "trailText",fieldType = OptionType(StringType), resolve = _.value.json.get("trailText").flatMap(_.asOpt[String])),
      Field(name = "group",fieldType = OptionType(StringType), resolve = _.value.json.get("group").flatMap(_.asOpt[String])),
      Field(name = "imageSrc",fieldType = OptionType(StringType), resolve = _.value.json.get("imageSrc").flatMap(_.asOpt[String])),
      Field(name = "imageSrcWidth",fieldType = OptionType(StringType), resolve = _.value.json.get("imageSrcWidth").flatMap(_.asOpt[String])),
      Field(name = "imageSrcHeight",fieldType = OptionType(StringType), resolve = _.value.json.get("imageSrcHeight").flatMap(_.asOpt[String])),
      Field(name = "isBreaking", fieldType = OptionType(BooleanType), resolve = _.value.json.get("isBreaking").flatMap(_.asOpt[Boolean])),
      Field(name = "isBoosted", fieldType = OptionType(BooleanType), resolve = _.value.json.get("isBoosted").flatMap(_.asOpt[Boolean])),
      Field(name = "boostLevel",fieldType = OptionType(StringType), resolve = _.value.json.get("boostLevel").flatMap(_.asOpt[String])),
      Field(name = "isImmersive", fieldType = OptionType(BooleanType), resolve = _.value.json.get("isImmersive").flatMap(_.asOpt[Boolean])),
      Field(name = "imageHide", fieldType = OptionType(BooleanType), resolve = _.value.json.get("imageHide").flatMap(_.asOpt[Boolean])),
      Field(name = "imageReplace", fieldType = OptionType(BooleanType), resolve = _.value.json.get("imageReplace").flatMap(_.asOpt[Boolean])),
      Field(name = "showMainVideo", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showMainVideo").flatMap(_.asOpt[Boolean])),
      Field(name = "showKickerTag", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showKickerTag").flatMap(_.asOpt[Boolean])),
      Field(name = "showKickerSection", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showKickerSection").flatMap(_.asOpt[Boolean])),
      Field(name = "byline",fieldType = OptionType(StringType), resolve = _.value.json.get("byline").flatMap(_.asOpt[String])),
      Field(name = "showByline", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showByline").flatMap(_.asOpt[Boolean])),
      Field(name = "customKicker",fieldType = OptionType(StringType), resolve = _.value.json.get("customKicker").flatMap(_.asOpt[String])),
      Field(name = "showKickerCustom", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showKickerCustom").flatMap(_.asOpt[Boolean])),
      Field(name = "imageCutoutReplace", fieldType = OptionType(BooleanType), resolve = _.value.json.get("imageCutoutReplace").flatMap(_.asOpt[Boolean])),
      Field(name = "imageCutoutSrc",fieldType = OptionType(StringType), resolve = _.value.json.get("imageCutoutSrc").flatMap(_.asOpt[String])),
      Field(name = "imageCutoutSrcWidth",fieldType = OptionType(StringType), resolve = _.value.json.get("imageCutoutSrcWidth").flatMap(_.asOpt[String])),
      Field(name = "imageCutoutSrcHeight",fieldType = OptionType(StringType), resolve = _.value.json.get("imageCutoutSrcHeight").flatMap(_.asOpt[String])),
      Field(name = "showBoostedHeadline", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showBoostedHeadline").flatMap(_.asOpt[Boolean])),
      Field(name = "showQuotedHeadline", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showQuotedHeadline").flatMap(_.asOpt[Boolean])),
      Field(name = "excludeFromRss", fieldType = OptionType(BooleanType), resolve = _.value.json.get("excludeFromRss").flatMap(_.asOpt[Boolean])),
      Field(name = "imageSlideshowReplace", fieldType = OptionType(BooleanType), resolve = _.value.json.get("imageSlideshowReplace").flatMap(_.asOpt[Boolean])),
      Field(name = "showLivePlayable", fieldType = OptionType(BooleanType), resolve = _.value.json.get("showLivePlayable").flatMap(_.asOpt[Boolean])),
      Field(name = "hideShowMore", fieldType = OptionType(BooleanType), resolve = _.value.json.get("hideShowMore").flatMap(_.asOpt[Boolean])),
      Field(name = "atomId",fieldType = OptionType(StringType), resolve = _.value.json.get("atomId").flatMap(_.asOpt[String])),
      Field(name = "blockId",fieldType = OptionType(StringType), resolve = _.value.json.get("blockId").flatMap(_.asOpt[String])),
      Field(name = "slideshow",  fieldType = OptionType(ListType(SlideshowAssetType)), resolve = _.value.json.get("slideshow").flatMap(_.asOpt[List[SlideshowAsset]]).filter(_.nonEmpty)),
      Field(name = "imageSource",  fieldType = OptionType(ImageSourceAssetType), resolve = _.value.json.get("imageSource").flatMap(_.asOpt[ImageSourceAsset]))
    ))

  // Expect "internal-code/page/123"
  private def getPageCodeFromTrailId(id: String): Long = id.split("/").lastOption.flatMap(_.toLongOption).getOrElse {
    throw new Exception(s"Page code $id was in an unexpected format")
  }
  private val TrailType = deriveObjectType[GQLQueryContext, Trail](
    ReplaceField("meta", Field(name = "meta", fieldType = OptionType(TrailMetadataType), resolve = _.value.meta)),
    AddFields(
      Field(
        name = "content",
        fieldType = OptionType(Content.Content),
        resolve = ctx => articlesByPageCode.deferOpt(getPageCodeFromTrailId(ctx.value.id))
      )
    )
  )


  private val SupportingItemMetadataType = deriveObjectType[GQLQueryContext, SupportingItemMetaData](
    ReplaceField("json", Field("json", StringType, resolve = _.value.json.toString))
  )
  private val SupportingItemType = deriveObjectType[GQLQueryContext, SupportingItem](
    ReplaceField("meta", Field("meta", OptionType(SupportingItemMetadataType), resolve= _.value.meta))
  )
  private val SlideshowAssetType = deriveObjectType[GQLQueryContext, SlideshowAsset]()
  private val ImageSourceAssetType = deriveObjectType[GQLQueryContext, ImageSourceAsset]()

  val Collection = ObjectType[GQLQueryContext, FaciaCollection](
    "Collection",
    () => fields[GQLQueryContext, FaciaCollection](
      Field(
        name = "id",
        fieldType = StringType,
        description = Some("The collection id"),
        resolve = ctx => ctx.value.id
      ),
      Field(
        name = "displayName",
        fieldType = StringType,
        description = Some("The name of the collection as it is rendered"),
        resolve = ctx => ctx.value.displayName
      ),
      Field(
        name = "href",
        fieldType = StringType,
        description = Some("Frankly, I'm unsure"),
        resolve = ctx => ctx.value.href.getOrElse("")
      ),
      Field(
        name = "live",
        fieldType = ListType(TrailType),
        description = Some("The live content for the collection"),
        resolve = ctx => ctx.value.live
      ),
      Field(
        name = "draft",
        fieldType = OptionType(ListType(TrailType)),
        description = Some("The draft content for the collection"),
        resolve = ctx => ctx.value.draft
      ),
      Field(
        name = "treats",
        fieldType = ListType(TrailType),
        description = Some("The treat content for the collection. Treat content is displayed to the side of the collection"),
        resolve = ctx => ctx.value.treats
      ),
      Field(
        name = "lastUpdated",
        fieldType = OptionType(StringType),
        description = Some("The last time the collection was updated"),
        resolve = ctx => ctx.value.lastUpdated.map(_.toString)
      ),
      Field(
        name = "updatedBy",
        fieldType = OptionType(StringType),
        description = Some("The user who last updated the collection"),
        resolve = ctx => ctx.value.updatedBy
      ),
      Field(
        name = "updatedEmail",
        fieldType = OptionType(StringType),
        description = Some("The e-mail of the user who last updated the collection"),
        resolve = ctx => ctx.value.updatedBy
      ),
      Field(
        name = "targetedTerritory",
        fieldType = OptionType(StringType),
        description = Some("The territory targeted by this collection. Used by platforms to conditionally display content in certain territories"),
        resolve = ctx => ctx.value.targetedTerritory.map(_.toString)
      ),
    )
  )
}
