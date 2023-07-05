package com.gu.contentapi.porter.graphql

import sangria.macros.derive._
import sangria.schema._

import java.time.format.DateTimeFormatter
import io.circe.generic.auto._
import io.circe.syntax._
import com.gu.contentapi.porter.model

object Tags {
  import Content.Reference
  import Content.Sponsorshup

  implicit val PodcastCategory = deriveObjectType[Unit, model.PodcastCategory]()
  implicit val TagPodcast = deriveObjectType[Unit, model.TagPodcast]()

  val Tag = deriveObjectType[Unit, model.Tag](
    ReplaceField("type", Field("type", OptionType(TagQueryParameters.TagTypes), resolve=_.value.`type`)),
    ReplaceField("alternateIds", Field("alternateIds", ListType(StringType), arguments = AlternateIdParameters.AllAlternateIdParameters, resolve= AlternateIdParameters.TagResolver[Unit])),
    ReplaceField("tagCategories", Field("tagCategories", ListType(StringType), resolve = _.value.tagCategories.map(_.toSeq).getOrElse(Seq()))),
    ReplaceField("entityIds", Field("entityIds", ListType(StringType), resolve = _.value.tagCategories.map(_.toSeq).getOrElse(Seq())))
  )
}
