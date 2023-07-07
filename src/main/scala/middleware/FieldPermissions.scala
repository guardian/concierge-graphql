package middleware

import datastore.GQLQueryContext
import middleware.FieldPermissions.restrictions
import org.slf4j.LoggerFactory
import sangria.execution.{BeforeFieldResult, Middleware, MiddlewareAfterField, MiddlewareBeforeField, MiddlewareQueryContext}
import sangria.schema.Context
import security.{InternalTier, RightsManagedTier, UserTier}

import scala.util.Try

class FieldPermissions extends Middleware[GQLQueryContext] with MiddlewareAfterField[GQLQueryContext] {
  private val logger = LoggerFactory.getLogger(getClass)

  override type QueryVal = this.type
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[GQLQueryContext, _, _]): FieldPermissions.this.type = {
    this
  }

  override def afterQuery(queryVal: FieldPermissions.this.type, context: MiddlewareQueryContext[GQLQueryContext, _, _]): Unit = {

  }

  override def beforeField(queryVal: FieldPermissions.this.type, mctx: MiddlewareQueryContext[GQLQueryContext, _, _], ctx: Context[GQLQueryContext, _]): BeforeFieldResult[GQLQueryContext, FieldVal] = {
    val fieldName = s"${ctx.parentType.name}.${ctx.field.name}"

    val isAllowed = restrictions.get(fieldName) match {
      case Some(restrictionLevel) =>
        !(ctx.ctx.userTier < restrictionLevel)
      case None =>
        true
    }

    if (isAllowed) {
      continue
    } else {
      throw Errors.PermissionDenied("This field is not permitted for your user tier")
    }
  }

  override def afterField(queryVal: FieldPermissions.this.type, fieldVal: Unit, value: Any, mctx: MiddlewareQueryContext[GQLQueryContext, _, _], ctx: Context[GQLQueryContext, _]): Option[Any] = {
    if(ctx.field.name=="channels" && ctx.ctx.userTier < InternalTier) {
      //if we are _not_ in Internal tier, only show information about Open channel
      try {
        val channelsData = value.asInstanceOf[Seq[com.gu.contentapi.porter.model.ContentChannel]]
        Some(channelsData.filter(_.channelId=="open"))
      } catch {
        case err:Throwable=>
          logger.error(s"Could not apply restrictions to channels: ${err.getMessage}", err)
          None
      }
    } else {
      Some(value)
    }
  }
}

object FieldPermissions {
  def singleton = new FieldPermissions

  //All fields are open, but the ones below are only available to tiers equal to or higher than those given
  val restrictions:Map[String, UserTier] = Map(
    "Content.debug" -> InternalTier,
    "Content.contentAliases"->InternalTier,
    "Content.rights" -> RightsManagedTier,
  )
}