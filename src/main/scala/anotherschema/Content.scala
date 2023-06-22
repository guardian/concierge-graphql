package anotherschema

import io.circe.{Json, JsonObject}
import io.circe.syntax._
import sangria.schema._
import io.circe.generic.auto._
import com.sksamuel.elastic4s.circe._
import datastore.DocumentRepo
import sangria.marshalling.circe._

object Content extends CirceHelpers {
  val ContentTypeEnum = EnumType(
    "ContentType",
    Some("What specific type of content does this document represent"),
    List(
      EnumValue("article",
        value="article",
        description = Some("A standard text article"))
    )
  )

  val definition:ObjectType[Unit, Json] = ObjectType(
    "Article",
    "The base type that all content derives from",
    ()=> fields[Unit, Json](
      Field("id", OptionType(StringType), Some("The content api ID (path)"), resolve = (ctx)=> getString(ctx, "id")),
      Field("type", OptionType(ContentTypeEnum), Some("What type of content is this document"), resolve = ctx => getString(ctx, "type")),
      Field("alternateIds", ListType(StringType), Some("Alternate IDs for this article"), resolve = ctx => getStringList(ctx, "alternateIds")),
      Field("webTitle", OptionType(StringType),
        Some("The title of the document, for web purposes. Normally (but not always) the same as the headline"),
        resolve = ctx => getString(ctx, "webTitle")
      ),
      Field("webPublicationDate", OptionType(StringType), Some("When was this published"), resolve = ctx => getString(ctx,"webPublicationDate")),
      Field("sectionId", OptionType(StringType), Some("Which section does this belong to"), resolve = ctx => getString(ctx,"sectionId")),
      Field("debug", OptionType(anotherschema.content.Debug.definition), Some("Internal debugging information"), resolve = ctx => (ctx.value \\ "debug").headOption),
    )
  )

  val ContentIdArg = Argument("id", StringType, description = "content API ID to look up")


  val Query = ObjectType[DocumentRepo, Unit](
    "Query", fields[DocumentRepo, Unit](
      Field("article", definition,
        arguments = ContentIdArg :: Nil,
        resolve = ctx=> ctx.ctx.docById(ctx arg ContentIdArg)
      )
    )
  )

  val ContentSchema = Schema(Query)
}
