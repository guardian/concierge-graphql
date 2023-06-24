package anotherschema

import anotherschema.content.Blocks
import io.circe.{Json, JsonObject}
import io.circe.syntax._
import sangria.schema._
import io.circe.generic.auto._
import com.sksamuel.elastic4s.circe._
import datastore.DocumentRepo
import org.slf4j.LoggerFactory
import sangria.marshalling.circe._

import scala.concurrent.ExecutionContext.Implicits.global

object Content extends SchemaDefinition with CirceHelpers {
  private val logger = LoggerFactory.getLogger(getClass)
  val ContentTypeEnum = EnumType(
    "ContentType",
    Some("What specific type of content does this document represent"),
    List(
      EnumValue("article",
        value="article",
        description = Some("A standard text article")),
      EnumValue("video",
        value="video",
        description = Some("A video, with minimal text content")),
      EnumValue("liveblog",
        value="liveblog",
        description = Some("A blog format that constantly updates")),
      EnumValue("gallery",
        value="gallery",
        description = Some("A gallery of pictures")
      )
    )
  )

  private val docId = io.circe.optics.JsonPath.root.id.string
  private val altIds = io.circe.optics.JsonPath.root.alternateIds.each.string
  private val docScore = io.circe.optics.JsonPath.root.score.double

  val definition:ObjectType[Unit, Json] = ObjectType(
    "Article",
    "The base type that all content derives from",
    ()=> fields[Unit, Json](

      Field("score", OptionType(FloatType), Some("The relevancy score of this hit to the query which you made"), resolve= ctx=> docScore.getOption(ctx.value)),
      Field("id", OptionType(StringType), Some("The content api ID (path)"), resolve = (ctx)=> docId.getOption(ctx.value).get),
      Field("type", OptionType(ContentTypeEnum), Some("What type of content is this document"), resolve = ctx => getString(ctx, "type")),
      Field("alternateIds", ListType(StringType), Some("Alternate IDs for this article"), resolve = ctx => altIds.getAll(ctx.value)),
      Field("webTitle", OptionType(StringType),
        Some("The title of the document, for web purposes. Normally (but not always) the same as the headline"),
        resolve = ctx => getString(ctx, "webTitle")
      ),
      Field("webPublicationDate", OptionType(StringType), Some("When was this published"), resolve = ctx => getString(ctx,"webPublicationDate")),
      Field("sectionId", OptionType(StringType), Some("Which section does this belong to"), resolve = ctx => getString(ctx,"sectionId")),
      Field("debug", OptionType(anotherschema.content.Debug.definition), Some("Internal debugging information"), resolve = ctx => (ctx.value \\ "debug").headOption),
      Field("blocks", anotherschema.content.Blocks.definition, None, resolve = ctx => (ctx.value \\ "blocks").head),
    )
  )

  val edge:ObjectType[Unit, Edge[Json]] = ObjectType(
    "ArticleEdge",
    "A list of articles with pagination features",
    ()=> fields[Unit, Edge[Json]](
      Field("totalCount", LongType, Some("Total number of results that match your query"), resolve= _.value.totalCount),
      Field("endCursor", OptionType(StringType), Some("The last record cursor in the set"), resolve = _.value.endCursor),
      Field("hasNextPage", BooleanType, Some("Whether there are any more records to retrieve"), resolve = _.value.hasNextPage),
      Field("nodes", ListType(definition), Some("The actual content returned"), resolve = _.value.nodes)
    )
  )

  import anotherschema.query.ContentQueryParameters._

  val Query = ObjectType[DocumentRepo, Unit](
    "Query", fields[DocumentRepo, Unit](
      Field("article", edge,
        arguments = AllContentQueryParameters,
        resolve = ctx=>
          (ctx arg ContentIdArg, ctx arg WebTitleArg) match {
            case (Some(contentId), _)=>
              ctx.ctx.docById (contentId)
            case (_, Some(webTitle))=>
              ctx.ctx
                .docsByWebTitle(webTitle, ctx arg OrderDate, ctx arg OrderBy)
            case _=>
              throw new RuntimeException("No fields given to search on")
          }
      )
    )
  )

  val ContentSchema = Schema(Query)
}
