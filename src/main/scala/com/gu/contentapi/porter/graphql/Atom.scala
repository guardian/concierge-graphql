package com.gu.contentapi.porter.graphql

import sangria.schema._
import sangria.macros.derive._
import com.gu.contentapi.porter.model
import datastore.GQLQueryContext
import io.circe.Json
import io.circe.optics.JsonPath
import io.circe.syntax._
import io.circe.generic.auto._

object Atom {
  val SimpleAtom = deriveObjectType[Unit, model.SimpleAtom]()

  val AtomChangeDetails = ObjectType[Unit, Json](
    "ChangeDetails",
    "Details of who changed something and when",
    ()=>fields[Unit, Json](
      Field("date",OptionType(StringType),resolve= ctx=>JsonPath.root.date.json.getOption(ctx.value).flatMap(DateTime.fromJsonFormatted)),
      Field("user", OptionType(Content.User),resolve=
        ctx=>JsonPath.root.user.json.getOption(ctx.value)
          .flatMap(json=>json.as[com.gu.contentapi.porter.model.User].toOption)),
    )
  )

  val ContentChangeDetails = ObjectType[Unit, Json](
    "ContentChangeDetails",
    "Details of when the content was changed",
    ()=>fields[Unit, Json](
      Field("created", OptionType(AtomChangeDetails), resolve=ctx=>JsonPath.root.created.json.getOption(ctx.value)),
      Field("embargo", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.embargo.json.getOption(ctx.value)),
      Field("expiry", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.expiry.json.getOption(ctx.value)),
      Field("lastModified", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.lastModified.json.getOption(ctx.value)),
      Field("published", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.published.json.getOption(ctx.value)),
      Field("scheduledLaunch", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.scheduledLaunch.json.getOption(ctx.value)),
      Field("revision", LongType, resolve = ctx=>JsonPath.root.revision.long.getOption(ctx.value).getOrElse(-1L)),
      Field("takenDown", OptionType(AtomChangeDetails), resolve = ctx => JsonPath.root.takenDown.json.getOption(ctx.value)),
    )
  )

  val Atom = ObjectType[GQLQueryContext, Json](
    "Atom",
    "A content atom, i.e. a piece of content that is embeddable within an article but with its own lifecycle",
    ()=>fields[GQLQueryContext,Json](
      Field("alternateIds",ListType(StringType), None,
        resolve=
          ctx=>JsonPath.root.alternateIds.arr
            .getOption(ctx.value)
            .map(values=>values.map(_.asString).collect({case Some(str)=>str}))
            .getOrElse(Seq())
      ),
      Field("atomType", StringType, None, resolve= ctx=>JsonPath.root.atomType.string.getOption(ctx.value).getOrElse("atom")),
      Field("commissioningDesks", OptionType(ListType(StringType)), None, resolve=
        ctx=> JsonPath.root.commissioningDesks.arr
          .getOption(ctx.value)
          .map(values=>values.map(_.asString).collect({case Some(str)=>str}))
          .getOrElse(Seq())
      ),
      Field("existsIn", RootQuery.ArticleEdge,
        description = Some("Search for articles that embed this atom"),
        arguments = ContentQueryParameters.AllContentQueryParameters,
        resolve = ctx=> ctx.ctx.repo.marshalledDocs(ctx arg ContentQueryParameters.QueryString, ctx arg ContentQueryParameters.QueryFields,
          JsonPath.root.id.string.getOption(ctx.value),
          ctx arg ContentQueryParameters.TagArg, ctx arg ContentQueryParameters.ExcludeTagArg,
          ctx arg ContentQueryParameters.SectionArg, ctx arg ContentQueryParameters.ExcludeSectionArg,
          ctx arg PaginationParameters.OrderDate, ctx arg PaginationParameters.OrderBy, ctx arg PaginationParameters.Limit, ctx arg PaginationParameters.Cursor)
      ),
      Field("contentChangeDetails", OptionType(ContentChangeDetails),
        description = Some("Details of significant events in the object lifecycle"),
        resolve = ctx => JsonPath.root.contentChangeDetails.json.getOption(ctx.value)),
      Field("data", OptionType(StringType),
        description = Some("Type-specific JSON encoded data for the atom"),
        resolve = ctx => JsonPath.root.data.json.getOption(ctx.value).map(_.noSpaces))
    )

  )
}
