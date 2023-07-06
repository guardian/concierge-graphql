package deprecated.anotherschema.content.blocks

import deprecated.anotherschema.{CirceHelpers, SchemaDefinition}
import io.circe.Json
import sangria.schema.{Field, ObjectType, OptionType, StringType, fields}

@deprecated("you should be using com.gu.contentapi.porter.graphql")
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
