package com.gu.contentapi.porter.graphql

import sangria.schema._

object AtomQueryParameters {
  val AtomTypes = EnumType(
    "AtomType",
    Some("Types of atoms available in the system"),
    List(
      EnumValue("quiz",None,"quiz"),
      EnumValue("media", None, "media"),
      EnumValue("chart", None, "chart"),
      EnumValue("explainer", None, "explainer"),
      EnumValue("qanda", None, "qanda"),
      EnumValue("profile", None, "profile"),
      EnumValue("timeline", None, "timeline"),
      EnumValue("cta", None, "cta"),
      EnumValue("guide", None, "guide"),
      EnumValue("audio", None, "audio"),
    )
  )
  val AtomIds = Argument("atomIds",OptionInputType(ListInputType(StringType)),description="list only atoms with one of these IDs")
  val QueryString = Argument("q", OptionInputType(StringType), description = "optional query string")
  val QueryFields = Argument("queryFields", OptionInputType(ListInputType(StringType)), description = "fields to perform a query against. Defaults to atom title and labels.")
  val AtomType = Argument("type", OptionInputType(AtomTypes), description = "only return atoms of this type")
  val RevisionBefore = Argument("revisionBefore", OptionInputType(LongType), description = "only return atoms which have a revision number before this value")
  val RevisionAfter = Argument("revisionAfter", OptionInputType(LongType), description = "only return atoms which have a revision number after this value")

  val AllParameters = AtomIds :: QueryString :: QueryFields :: AtomType :: RevisionBefore :: RevisionAfter :: PaginationParameters.OrderBy :: PaginationParameters.Limit :: PaginationParameters.Cursor :: Nil
}
