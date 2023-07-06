package deprecated.anotherschema.query

import sangria.schema.{EnumType, EnumValue}
@deprecated("you should be using com.gu.contentapi.porter.graphql")
object OrderDate {
  val definition = EnumType(
    "OrderDate",
    Some("Which date field to use for ordering the content"),
    List(
      EnumValue("published", Some("When the content was published to web"), "webPublicationDate"),
      EnumValue("firstPublished", Some("When the first version of this content was published"), "fields.firstPublicationDate"),
      EnumValue("lastModified", Some("The last time the content was modified prior to publication"), "fields.lastModified"),
      EnumValue("newspaperEdition", Some("The date that this was published in the newspaper (can be null)"), "fields.newspaperEditionDate"),
      EnumValue("scheduledPublication", Some("When the article is scheduled to be launched"), "fields.scheduledPublicationDate"),
      EnumValue("lastIndexed", Some("When the content was last indexed"), "debug.lastSeenByPorterAt")  //Note - this defaults to webPublicationDate if the user tier is not internal, in the existing concierge
    )
  )
}
