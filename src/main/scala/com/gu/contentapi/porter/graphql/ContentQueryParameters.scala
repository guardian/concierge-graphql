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
  val TagArg = Argument("tags", OptionInputType(ListInputType(StringType)), description = "look up articles associated with all of these tag IDs")
  val ExcludeTagArg = Argument("excludeTags", OptionInputType(ListInputType(StringType)), description = "don't include any articles with these tag IDs")
  val SectionArg = Argument("sectionId", OptionInputType(ListInputType(StringType)), description = "look up articles in any of these sections")
  val ExcludeSectionArg = Argument("excludeSections", OptionInputType(ListInputType(StringType)), description = "don't include any articles with these tag IDs")

  val AllContentQueryParameters = ContentIdArg :: QueryString :: QueryFields :: TagArg :: ExcludeTagArg :: SectionArg :: ExcludeSectionArg :: OrderBy :: OrderDate :: Cursor :: Limit :: Nil
}
