package com.gu.contentapi.porter.graphql

import sangria.schema._

object FrontQueryParameters {
  val FrontIds = Argument("frontIds",OptionInputType(ListInputType(StringType)),description="list only fronts with one of these IDs")
  val AllParameters = FrontIds :: Nil
}
