package deprecated.anotherschema.content

import deprecated.anotherschema.CirceHelpers
import io.circe.Json
import sangria.schema._
@deprecated("you should be using com.gu.contentapi.porter.graphql")
object Debug extends CirceHelpers {

  val definition = ObjectType(
    "Debug",
    "Internal debugging information",
    ()=>fields[Unit, Json](
      Field("contentSource", OptionType(StringType), Some("Where did this come from"), resolve = ctx => getString(ctx, "contentSource")),
      Field("lastSeenByApiIndexerAt", OptionType(StringType), Some(""), resolve = ctx => getString(ctx, "lastSeenByApiIndexerAt")),
      Field("lastSeenByAttendantAt", OptionType(StringType), Some(""), resolve = ctx => getString(ctx, "lastSeenByAttendantAt")),
      Field("lastSeenByPorterAt", OptionType(StringType), Some(""), resolve = ctx => getString(ctx, "lastSeenByPorterAt")),
      Field("originatingSystem", OptionType(StringType), Some(""), resolve = ctx => getString(ctx, "originatingSystem")),
      Field("revisionSeenByApiIndexer", OptionType(LongType), Some(""), resolve = ctx => getLong(ctx, "revisionSeenByApiIndexer")),
      Field("revisionSeenByPorter", OptionType(LongType), Some(""), resolve = ctx => getLong(ctx, "revisionSeenByPorter")),
    )
  )
}
