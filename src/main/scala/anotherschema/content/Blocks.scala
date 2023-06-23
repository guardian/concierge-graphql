package anotherschema.content

import anotherschema.SchemaDefinition
import anotherschema.content.blocks.Body
import io.circe.Json
import sangria.schema.{Field, ObjectType, fields}

object Blocks extends SchemaDefinition {
  override val definition: ObjectType[Unit, Json] = ObjectType(
    "Blocks",
    "The bits that make up the article",
    () => fields[Unit, Json](
      Field("body", Body.definition, Some("The main content of the article"), resolve = ctx => (ctx.value \\ "body").head)
    )
  )
}
