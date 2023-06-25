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

  val Limit = Argument("pageSize", OptionInputType(IntType), description = s"return this many results at once. Default is 25")
  val AllContentQueryParameters = ContentIdArg :: WebTitleArg :: OrderBy :: OrderDate :: Limit:: Nil
}
