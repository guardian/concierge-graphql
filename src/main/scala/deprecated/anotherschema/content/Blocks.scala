package deprecated.anotherschema.content

import deprecated.anotherschema.{CirceHelpers, SchemaDefinition}
import deprecated.anotherschema.content.blocks.Body
import io.circe.Json
import io.circe.optics.JsonPath
import sangria.schema.{Field, ListType, ObjectType, fields}
@deprecated("you should be using com.gu.contentapi.porter.graphql")
object Blocks extends SchemaDefinition with CirceHelpers {
  override val definition: ObjectType[Unit, Json] = ObjectType(
    "Blocks",
    "The bits that make up the article",
    () => fields[Unit, Json](
      Field("body",
        ListType(Body.definition),
        Some("The main content of the article"),
        resolve = ctx => JsonList(JsonPath.root.body.arr.getOption(ctx.value).getOrElse(Vector())))
    )
  )
}
