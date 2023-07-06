package schema.content

import java.time.ZonedDateTime

object Blocks {
  case class Attributes(
                         keyEvent:Option[String],
                         membershipPlaceholder:Option[Map[String,Any]],
                         pinned: Option[String],
                         summary: Option[String],
                         title:Option[String]
                       )

  case class UserTrace(email:String, firstName:String, lastName:String)

  case class BodyBlock(
                      attributes: Option[Attributes],
                      bodyHtml: String,
                      bodyTextSummary: Option[String],
                      contributors: Seq[String],
                      createdBy: UserTrace,
                      createdDate: ZonedDateTime,

                      )
}
