package com.gu.contentapi.porter.graphql

import sangria.macros.derive._
import sangria.schema._

import java.time.format.DateTimeFormatter
import io.circe.generic.auto._
import io.circe.syntax._
import com.gu.contentapi.porter.model
import datastore.GQLQueryContext

object Tags {
  import Content.Reference
  import Content.Sponsorshup

  implicit val PodcastCategory = deriveObjectType[Unit, model.PodcastCategory]()
  implicit val TagPodcast = deriveObjectType[Unit, model.TagPodcast]()

  val Tag = deriveObjectType[GQLQueryContext, model.Tag](
    ReplaceField("type", Field("type", OptionType(TagQueryParameters.TagTypes), resolve=_.value.`type`)),
    ReplaceField("alternateIds", Field("alternateIds", ListType(StringType), arguments = AlternateIdParameters.AllAlternateIdParameters, resolve= AlternateIdParameters.TagResolver[GQLQueryContext])),
    ReplaceField("tagCategories", Field("tagCategories", ListType(StringType), resolve = _.value.tagCategories.map(_.toSeq).getOrElse(Seq()))),
    ReplaceField("entityIds", Field("entityIds", ListType(StringType), resolve = _.value.tagCategories.map(_.toSeq).getOrElse(Seq()))),
    AddFields(
      Field("matchingContent", RootQuery.ArticleEdge, description=Some("Articles and other content which have this tag"),
        arguments= ContentQueryParameters.AllContentQueryParameters.filterNot(_ == ContentQueryParameters.TagArg),
        resolve = { ctx=>
          ctx.ctx.repo.marshalledDocs(ctx arg ContentQueryParameters.QueryString,
            queryFields=ctx arg ContentQueryParameters.QueryFields,
            atomId = None,
            forChannel = ctx arg ContentQueryParameters.ChannelArg,
            userTier = ctx.ctx.userTier,
            tagIds = Some(Seq(ctx.value.id)),
            excludeTags = ctx arg ContentQueryParameters.ExcludeTagArg,
            sectionIds = ctx arg ContentQueryParameters.SectionArg,
            excludeSections = ctx arg ContentQueryParameters.ExcludeSectionArg,
            orderDate = ctx arg PaginationParameters.OrderDate,
            orderBy = ctx arg PaginationParameters.OrderBy,
            limit = ctx arg PaginationParameters.Limit,
            cursor = ctx arg PaginationParameters.Cursor,
          )
        })
    )
  )
}
