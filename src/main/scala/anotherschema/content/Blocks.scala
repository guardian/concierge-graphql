package anotherschema.content

import anotherschema.{CirceHelpers, SchemaDefinition}
import anotherschema.content.blocks.Body
import io.circe.Json
import io.circe.optics.JsonPath
import sangria.schema.{Field, ListType, ObjectType, fields}

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
