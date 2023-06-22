package schema

import datastore.DocumentRepo
import sangria.schema._
import schema.content.Debug.Debug

object Content {
  object ContentTypes extends Enumeration {
    val article = Value
  }
  type ContentType = ContentTypes.Value

  val ContentTypeEnum = EnumType(
    "ContentType",
    Some("What specific type of content does this document represent"),
    List(
      EnumValue("article",
        value="article",
        description = Some("A standard text article"))
    )
  )

  case class Document(
                      id:String,
                      `type`: ContentType,
                      alternateIds: Seq[String],
                      webTitle: String,
                      webPublicationDate: String,
                      sectionId: String,
                      debug: schema.content.Debug.Debug,
                    )

  val DocumentDefn: ObjectType[Unit, Content.Document] =
    ObjectType(
      "Article",
      "The base type that all content derives from",
      ()=> fields[Unit, Content.Document](
        Field("id", StringType, Some("The content api ID (path)"), resolve = _.value.id),
        Field("type", ContentTypeEnum, Some("What type of content is this document"), resolve = _.value.`type`.toString),
        Field("alternateIds", ListType(StringType), Some("Alternate IDs for this article"), resolve = _.value.alternateIds),
        Field("webTitle", StringType,
          Some("The title of the document, for web purposes. Normally (but not always) the same as the headline"),
          resolve = _.value.webTitle),
        Field("webPublicationDate", StringType, Some("When was this published"), resolve = _.value.webPublicationDate),
        Field("sectionId", StringType, Some("Which section does this belong to"), resolve = _.value.sectionId),
        Field("debug", schema.content.Debug.definition, Some("Internal debugging information"), resolve = _.value.debug)
      )
    )


  val ContentIdArg = Argument("id", StringType, description = "content API ID to look up")

  val Query = ObjectType[DocumentRepo, Unit](
    "Query", fields[DocumentRepo, Unit](
      Field("article", DocumentDefn,
        arguments = ContentIdArg :: Nil,
        resolve = ctx=> ctx.ctx.docById(ctx arg ContentIdArg)
      )
    )
  )

  val ContentSchema = Schema(Query)
}
