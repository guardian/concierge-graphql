package com.gu.contentapi.porter.graphql

import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import sangria.schema._
object PaginationParameters {
  object OrderBySchema {
    val definition = EnumType(
      "Ordering",
      Some("How to sort the results"),
      List(
        EnumValue("newest", None, SortOrder.DESC),
        EnumValue("oldest", None, SortOrder.ASC)
      )
    )
  }

  object OrderDateSchema {
    val definition = EnumType(
      "OrderDate",
      Some("Which date field to use for ordering the content, or whether to search on document score"),
      List(
        EnumValue("score", Some("Ignore when the content was made or published and sort by relevance to the query parameters"), "score"),
        EnumValue("published", Some("When the content was published to web"), "webPublicationDate"),
        EnumValue("firstPublished", Some("When the first version of this content was published"), "fields.firstPublicationDate"),
        EnumValue("lastModified", Some("The last time the content was modified prior to publication"), "fields.lastModified"),
        EnumValue("newspaperEdition", Some("The date that this was published in the newspaper (can be null)"), "fields.newspaperEditionDate"),
        EnumValue("scheduledPublication", Some("When the article is scheduled to be launched"), "fields.scheduledPublicationDate"),
        EnumValue("lastIndexed", Some("When the content was last indexed"), "debug.lastSeenByPorterAt") //Note - this defaults to webPublicationDate if the user tier is not internal, in the existing concierge
      )
    )
  }

  val OrderBy = Argument("orderBy", OptionInputType(OrderBySchema.definition), description = "whether to order ascending or descending")
  val OrderDate = Argument("orderDate", OptionInputType(OrderDateSchema.definition), description = "choose a field to sort the results on")
  val Cursor = Argument("cursor", OptionInputType(StringType), description = "To continue a search, pass the value from `endCursor` in this argument")
  val Limit = Argument("limit", OptionInputType(IntType), description = "The maximum number of results to return")

}
