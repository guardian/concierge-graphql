package anotherschema.query

import sangria.schema.{Argument, IntType, OptionInputType, StringType}

/**
 * These are all the things you can search on
 */
object ContentQueryParameters {
  val ContentIdArg = Argument("id", OptionInputType(StringType), description = "get one article by ID")
  val WebTitleArg = Argument("webTitle", OptionInputType(StringType), description = "look up many articles by web title")

  val OrderBy = Argument("orderBy", OptionInputType(anotherschema.query.OrderBy.definition), description = "how to sort the results")
  val OrderDate = Argument("orderDate", OptionInputType(anotherschema.query.OrderDate.definition), description = "field to use for sorting the results")
  val Cursor = Argument("cursor", OptionInputType(StringType), description="To continue a search, pass the value from `endCursor` in this argument")
  val Limit = Argument("limit", OptionInputType(IntType), description = "The maximum number of results to return")


  val AllContentQueryParameters = ContentIdArg :: WebTitleArg :: OrderBy :: OrderDate :: Cursor :: Limit :: Nil
}
