package schema.content

import sangria.schema._

object Debug {
  case class Debug(
                  contentSource: String,
                  lastSeenByApiIndexerAt: String,
                  lastSeenByAttendantAt: String,
                  lastSeenByPorterAt: String,
                  originatingSystem: String,
                  revisionSeenByApiIndexer: Long,
                  revisionSeenByPorter: Long,
                  )

  val definition = ObjectType[Unit, Debug](
    "Debug",
    "Internal debugging information",
    ()=>fields[Unit, Debug](
      Field("contentSource", StringType, Some("Where did this come from"), resolve = _.value.contentSource),
      Field("lastSeenByApiIndexerAt", StringType, Some(""), resolve = _.value.lastSeenByApiIndexerAt),
      Field("lastSeenByAttendantAt", StringType, Some(""), resolve = _.value.lastSeenByAttendantAt),
      Field("lastSeenByPorterAt", StringType, Some(""), resolve = _.value.lastSeenByPorterAt),
      Field("originatingSystem", StringType, Some(""), resolve = _.value.originatingSystem),
      Field("revisionSeenByApiIndexer", LongType, Some(""), resolve = _.value.revisionSeenByApiIndexer),
      Field("revisionSeenByPorter", LongType, Some(""), resolve = _.value.revisionSeenByPorter),
    )
  )
}
