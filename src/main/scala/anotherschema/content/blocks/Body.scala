package anotherschema.content.blocks

import anotherschema.{CirceHelpers, SchemaDefinition}
import io.circe.Json
import sangria.schema.{Field, ListType, ObjectType, OptionType, StringType, fields}

object Body extends SchemaDefinition with CirceHelpers {
  override val definition: ObjectType[Unit, Json] = ObjectType(
    "body",
    "The main content of the article",
    ()=>fields[Unit, Json](
      Field("bodyHtml", OptionType(StringType), Some("HTML formatted text of the content"), resolve=ctx=>getString(ctx, "bodyHtml")),
      Field("bodyTextSummary", OptionType(StringType), Some("Plain-text summary of the content"), resolve=ctx=>getString(ctx, "bodyTextSummary")),
      Field("contributors", ListType(StringType), Some("Contributors"), resolve=ctx=>getStringList(ctx, "contributors")),
      Field("createdBy", OptionType(UserTrace.definition), Some("Who created this block"), resolve=ctx=>(ctx.value \\ "createdBy").headOption),
      Field("createdDate", OptionType(StringType), Some("When was this block created"), resolve=ctx=>getString(ctx, "createdDate")),

    )
  )
}
