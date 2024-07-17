package com.gu.contentapi.porter.graphql

import com.gu.contentapi.porter.model.{Content, Tag}
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import sangria.schema._
import datastore.GQLQueryContext
import deprecated.anotherschema.Edge
import io.circe.Json

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

object RootQuery {
  private val logger = LoggerFactory.getLogger(getClass)

  val ArticleEdge: ObjectType[Unit, Edge[Content]] = ObjectType(
    "ArticleEdge",
    "A list of articles with pagination features",
    () => fields[Unit, Edge[Content]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve = _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(com.gu.contentapi.porter.graphql.Content.Content), Some("The actual content returned"), resolve = _.value.nodes)
    )
  )

  val TagEdge: ObjectType[GQLQueryContext, Edge[Tag]] = ObjectType(
    "TagEdge",
    "A list of tags with pagination features",
    () => fields[GQLQueryContext, Edge[Tag]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve = _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(com.gu.contentapi.porter.graphql.Tags.Tag), Some("The actual tags returned"), resolve = _.value.nodes),
      Field("matchingAnyTag", ArticleEdge, Some("Content which matches any of the tags returned"),
        arguments= ContentQueryParameters.AllContentQueryParameters,
        resolve = { ctx=>
          if(ctx.value.nodes.isEmpty) {
            Future(Edge[Content](
              0L,
              None,
              false,
              Seq()
            ))
          } else {
            ctx.ctx.repo.marshalledDocs(ctx arg ContentQueryParameters.QueryString,
              queryFields = ctx arg ContentQueryParameters.QueryFields,
              atomId = None,
              forChannel = ctx arg ContentQueryParameters.ChannelArg,
              userTier = ctx.ctx.userTier,
              tagIds = Some(ctx.value.nodes.map(_.id)),
              excludeTags = ctx arg ContentQueryParameters.ExcludeTagArg,
              sectionIds = ctx arg ContentQueryParameters.SectionArg,
              excludeSections = ctx arg ContentQueryParameters.ExcludeSectionArg,
              orderDate = ctx arg PaginationParameters.OrderDate,
              orderBy = ctx arg PaginationParameters.OrderBy,
              limit = ctx arg PaginationParameters.Limit,
              cursor = ctx arg PaginationParameters.Cursor,
            )
          }
      })
    )
  )

  val AtomEdge: ObjectType[Unit, Edge[Json]] = ObjectType(
    "AtomEdge",
    "A list of atoms with pagination features",
    () => fields[Unit, Edge[Json]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve = _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(Atom.Atom), Some("The actual atoms returned"), resolve = _.value.nodes)
    )
  )

  private def contentTransform: Json => Content = _.as[Content] match {
    case Right(content) => content
    case Left(err) =>
      logger.error(s"Could not convert object from json: ", err)
      throw new RuntimeException("Unable to marshal data")
  }

  val Query = ObjectType[GQLQueryContext, Unit](
    "Query", fields[GQLQueryContext, Unit](
      Field("article", ArticleEdge,
        description = Some("An Article is the main unit of our publication.  You can search articles directly here, or query" +
          " tags or sections to see what articles live within it."),
        arguments = ContentQueryParameters.AllContentQueryParameters,
        resolve = ctx =>
          ctx arg ContentQueryParameters.ContentIdArg match {
            case Some(contentId) =>
              ctx.ctx.repo.docById(contentId).map(_.map(contentTransform))
            case None =>
              ctx.ctx.repo
                .marshalledDocs(ctx arg ContentQueryParameters.QueryString, ctx arg ContentQueryParameters.QueryFields,
                  None, ctx arg ContentQueryParameters.ChannelArg, ctx.ctx.userTier,
                  ctx arg ContentQueryParameters.TagArg, ctx arg ContentQueryParameters.ExcludeTagArg,
                  ctx arg ContentQueryParameters.SectionArg, ctx arg ContentQueryParameters.ExcludeSectionArg,
                  ctx arg PaginationParameters.OrderDate, ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor)
          }
      ),
      Field("tag", TagEdge,
        description = Some("The Guardian uses tags to group similar pieces of content together across multiple different viewpoints.  " +
          "Tags are a closed set, which can be searched here, and there are different types of tags which represent different viewpoints"),
        arguments = TagQueryParameters.AllTagQueryParameters,
        resolve = ctx =>
          ctx.ctx.repo.marshalledTags(ctx arg TagQueryParameters.QueryString,
            ctx arg TagQueryParameters.Fuzziness,
            ctx arg TagQueryParameters.tagId,
            ctx arg TagQueryParameters.Section,
            ctx arg TagQueryParameters.TagType,
            ctx arg TagQueryParameters.Category,
            ctx arg TagQueryParameters.Reference,
            ctx arg PaginationParameters.OrderBy,
            ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor)
      ),
      Field("atom", AtomEdge,
        description = Some("An Atom is a piece of content which can be linked to multiple articles but may have a production lifecycle independent" +
          " of these articles.  Examples are cartoons, videos, quizzes, call-to-action blocks, etc."),
        arguments = AtomQueryParameters.AllParameters,
        resolve = ctx=>
      ctx.ctx.repo.atoms(ctx arg AtomQueryParameters.AtomIds, ctx arg AtomQueryParameters.QueryString, ctx arg AtomQueryParameters.QueryFields,
        ctx arg AtomQueryParameters.AtomType, ctx arg AtomQueryParameters.RevisionBefore, ctx arg AtomQueryParameters.RevisionAfter,
        ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor))
    )
  )

  val schema = Schema(Query)
}
