package anotherschema.content.blocks

import anotherschema.content.blocks.element.ElementArguments
import anotherschema.{CirceHelpers, SchemaDefinition}
import io.circe.Json
import io.circe.optics.JsonPath
import org.slf4j.LoggerFactory
import sangria.schema.{BooleanType, Field, ListType, ObjectType, OptionType, StringType, fields}
import sangria.schema._
object Body extends SchemaDefinition with CirceHelpers {
  override val definition: ObjectType[Unit, Json] = ObjectType(
    "body",
    "The main content of the article",
    ()=>{
      fields[Unit, Json](
        Field("id", OptionType(StringType), Some("ID of the block"), resolve=ctx=>getString(ctx, "id")),
        Field("title", OptionType(StringType), Some("Title of the block"), resolve=ctx=>getString(ctx, "title")),
        Field("attributes", OptionType(StringType), Some("Raw json attributes"), resolve = ctx => JsonPath.root.attributes.json.getOption(ctx.value).map(_.noSpaces)),
        Field("published", OptionType(BooleanType), Some("Has this been published?"), resolve = ctx => getBool(ctx, "published")),
        Field("createdDate", OptionType(StringType), Some("Timestamp that the block was created"), resolve = ctx => getString(ctx, "createdDate")),
        Field("firstPublishedDate", OptionType(StringType), Some("When was this first published?"), resolve = ctx => getString(ctx, "firstPublishedDate")),
        Field("publishedDate", OptionType(StringType), Some("When was this last published?"), resolve = ctx => getString(ctx, "publishedDate")),
        Field("lastModifiedDate", OptionType(StringType), Some("When was this last modified?"), resolve = ctx => getString(ctx, "lastModifiedDate")),

        Field("bodyHtml", OptionType(StringType), Some("HTML formatted text of the content"), resolve=ctx=>getString(ctx, "bodyHtml")),
        Field("bodyTextSummary", OptionType(StringType), Some("Plain-text summary of the content"), resolve=ctx=>getString(ctx, "bodyTextSummary")),
        Field("contributors", ListType(StringType), Some("Contributors"), resolve=ctx=>getStringList(ctx, "contributors")),
        Field("createdBy", OptionType(UserTrace.definition), Some("Who created this block"), resolve=ctx=>(ctx.value \\ "createdBy").headOption),
        Field("lastModifiedBy", OptionType(UserTrace.definition), Some("Who last changed this block"), resolve=ctx=>(ctx.value \\ "lastModifiedBy").headOption),

        Field("elements",
          ListType(Element.definition),
          Some("Components of the block"),
          arguments=ElementArguments.AllElementArguments,
          resolve= ctx=>{
            ElementArguments.filterByType(
              JsonList(JsonPath.root.elements.arr.getOption(ctx.value).getOrElse(Vector())),
              ctx arg ElementArguments.ElementType,
            )
          }
        )
      )
    }
  )
}
