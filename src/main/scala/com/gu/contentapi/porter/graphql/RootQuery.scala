package com.gu.contentapi.porter.graphql

import com.gu.contentapi.porter.model.{Content, Tag}
import sangria.schema._
import datastore.GQLQueryContext
import deprecated.anotherschema.Edge
import io.circe.Json

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

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

  val TagEdge: ObjectType[Unit, Edge[Tag]] = ObjectType(
    "TagEdge",
    "A list of tags with pagination features",
    () => fields[Unit, Edge[Tag]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve = _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(com.gu.contentapi.porter.graphql.Tags.Tag), Some("The actual tags returned"), resolve = _.value.nodes)
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
        arguments = ContentQueryParameters.AllContentQueryParameters,
        resolve = ctx =>
          ctx arg ContentQueryParameters.ContentIdArg match {
            case Some(contentId) =>
              ctx.ctx.repo.docById(contentId).map(_.map(contentTransform))
            case None =>
              ctx.ctx.repo
                .marshalledDocs(ctx arg ContentQueryParameters.QueryString, ctx arg ContentQueryParameters.QueryFields,
                  None,
                  ctx arg ContentQueryParameters.TagArg, ctx arg ContentQueryParameters.ExcludeTagArg,
                  ctx arg ContentQueryParameters.SectionArg, ctx arg ContentQueryParameters.ExcludeSectionArg,
                  ctx arg PaginationParameters.OrderDate, ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor)
          }
      ),
      Field("tag", TagEdge,
        arguments = TagQueryParameters.AllTagQueryParameters,
        resolve = ctx =>
          ctx.ctx.repo.marshalledTags(ctx arg TagQueryParameters.tagId, ctx arg TagQueryParameters.Section, ctx arg TagQueryParameters.TagType, ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor)
      ),
      Field("atom", AtomEdge,
        arguments = AtomQueryParameters.AllParameters,
        resolve = ctx=>
      ctx.ctx.repo.atoms(ctx arg AtomQueryParameters.AtomIds, ctx arg AtomQueryParameters.QueryString, ctx arg AtomQueryParameters.QueryFields,
        ctx arg AtomQueryParameters.AtomType, ctx arg AtomQueryParameters.RevisionBefore, ctx arg AtomQueryParameters.RevisionAfter,
        ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor))
    )
  )

  val schema = Schema(Query)
}
