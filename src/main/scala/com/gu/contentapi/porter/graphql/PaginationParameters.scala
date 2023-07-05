package com.gu.contentapi.porter.graphql

import sangria.schema._
object PaginationParameters {
  val OrderBy = Argument("orderBy", OptionInputType(anotherschema.query.OrderBy.definition), description = "how to sort the results")
  val OrderDate = Argument("orderDate", OptionInputType(anotherschema.query.OrderDate.definition), description = "field to use for sorting the results")
  val Cursor = Argument("cursor", OptionInputType(StringType), description = "To continue a search, pass the value from `endCursor` in this argument")
  val Limit = Argument("limit", OptionInputType(IntType), description = "The maximum number of results to return")

}
