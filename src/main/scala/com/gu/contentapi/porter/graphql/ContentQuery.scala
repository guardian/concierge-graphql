package com.gu.contentapi.porter.graphql

import anotherschema.Edge
import com.gu.contentapi.porter.model.{Content, Tag}
import sangria.schema._
import datastore.DocumentRepo
import io.circe.Json

import scala.concurrent.ExecutionContext.Implicits.global
import io.circe.generic.auto._
import org.slf4j.LoggerFactory

object ContentQuery {
  private val logger = LoggerFactory.getLogger(getClass)
  import anotherschema.query.ContentQueryParameters._

  val Edge: ObjectType[Unit, Edge[Content]] = ObjectType(
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
    "A list of articles with pagination features",
    () => fields[Unit, Edge[Tag]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve = _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(com.gu.contentapi.porter.graphql.Tags.Tag), Some("The actual content returned"), resolve = _.value.nodes)
    )
  )

  private def contentTransform: Json => Content = _.as[Content] match {
    case Right(content) => content
    case Left(err) =>
      logger.error(s"Could not convert object from json: ", err)
      throw new RuntimeException("Unable to marshal data")
  }

  val Query = ObjectType[DocumentRepo, Unit](
    "Query", fields[DocumentRepo, Unit](
      Field("article", Edge,
        arguments = AllContentQueryParameters,
        resolve = ctx =>
          (ctx arg ContentIdArg, ctx arg WebTitleArg) match {
            case (Some(contentId), _) =>
              ctx.ctx.docById(contentId).map(_.map(contentTransform))
            case (_, Some(webTitle)) =>
              ctx.ctx
                .marshalledDocsByWebTitle(webTitle, ctx arg OrderDate, ctx arg OrderBy, ctx arg Limit, ctx arg Cursor)
            case _ =>
              throw new RuntimeException("No fields given to search on")
          }
      ),
      Field("tag", TagEdge,
        arguments = TagQueryParameters.AllTagQueryParameters,
        resolve = ctx =>
          ctx.ctx.marshalledTags(ctx arg TagQueryParameters.tagId, ctx arg TagQueryParameters.Section, ctx arg TagQueryParameters.TagType, ctx arg PaginationParameters.OrderBy, ctx arg Limit, ctx arg PaginationParameters.Cursor)
      )
    )
  )

  val schema = Schema(Query)
}
