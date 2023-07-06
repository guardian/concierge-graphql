package com.gu.contentapi.porter.graphql

import sangria.schema._
object PaginationParameters {
  val OrderBy = Argument("orderBy", OptionInputType(anotherschema.query.OrderBy.definition), description = "whether to order ascending or descending")
  val OrderDate = Argument("orderDate", OptionInputType(anotherschema.query.OrderDate.definition), description = "choose a field to sort the results on")
  val Cursor = Argument("cursor", OptionInputType(StringType), description = "To continue a search, pass the value from `endCursor` in this argument")
  val Limit = Argument("limit", OptionInputType(IntType), description = "The maximum number of results to return")

}
