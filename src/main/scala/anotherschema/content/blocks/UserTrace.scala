package anotherschema.content.blocks

import anotherschema.{CirceHelpers, SchemaDefinition}
import io.circe.Json
import sangria.schema.{Field, ObjectType, OptionType, StringType, fields}

object UserTrace extends SchemaDefinition with CirceHelpers {
  override val definition: ObjectType[Unit, Json] = ObjectType(
    "UserTrace",
    "A record of who did what",
    ()=>fields[Unit, Json](
      Field("email",OptionType(StringType), Some("email address"), resolve=ctx=>getString(ctx, "email")),
      Field("firstName",OptionType(StringType), None, resolve=ctx=>getString(ctx, "firstName")),
      Field("lastName",OptionType(StringType), None, resolve=ctx=>getString(ctx, "lastName")),
    )
  )
}
