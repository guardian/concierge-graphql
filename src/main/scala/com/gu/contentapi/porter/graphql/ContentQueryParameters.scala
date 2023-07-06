package com.gu.contentapi.porter.graphql

import sangria.schema.{Argument, IntType, ListInputType, ListType, OptionInputType, StringType}

/**
 * These are all the things you can search on
 */
object ContentQueryParameters {
  import PaginationParameters._

  val ContentIdArg = Argument("id", OptionInputType(StringType), description = "get one article by ID")
  val QueryString = Argument("q", OptionInputType(StringType), description = "an Elastic Search query string to search for content")
  val QueryFields = Argument("queryFields", OptionInputType(ListInputType(StringType)), description = "fields to perform a query against. Defaults to webTitle and path.")
  val TagArg = Argument("tagId", OptionInputType(ListInputType(StringType)), description = "look up articles associated with these tag IDs")
  val SectionArg = Argument("sectionId", OptionInputType(ListInputType(StringType)), description = "look up articles in this section")

  val AllContentQueryParameters = ContentIdArg :: QueryString :: QueryFields :: TagArg :: SectionArg :: OrderBy :: OrderDate :: Cursor :: Limit :: Nil
}
