package com.gu.contentapi.porter.graphql

import sangria.schema.{Argument, IntType, OptionInputType, StringType}

/**
 * These are all the things you can search on
 */
object ContentQueryParameters {
  import PaginationParameters._

  val ContentIdArg = Argument("id", OptionInputType(StringType), description = "get one article by ID")
  val WebTitleArg = Argument("webTitle", OptionInputType(StringType), description = "look up many articles by web title")


  val AllContentQueryParameters = ContentIdArg :: WebTitleArg :: OrderBy :: OrderDate :: Cursor :: Limit :: Nil
}
