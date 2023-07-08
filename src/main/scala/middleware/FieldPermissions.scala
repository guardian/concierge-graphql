package middleware

import datastore.GQLQueryContext
import org.slf4j.LoggerFactory
import sangria.execution.{BeforeFieldResult, Middleware, MiddlewareAfterField, MiddlewareBeforeField, MiddlewareQueryContext}
import sangria.schema.Context
import security.{InternalTier, RightsManagedTier, UserTier}

import scala.util.Try

class FieldPermissions extends Middleware[GQLQueryContext] with MiddlewareAfterField[GQLQueryContext] {
  private val logger = LoggerFactory.getLogger(getClass)

  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[GQLQueryContext, _, _]): Unit = {
  }

  override def afterQuery(queryVal: Unit, context: MiddlewareQueryContext[GQLQueryContext, _, _]): Unit = {

  }

  override def beforeField(queryVal: Unit, mctx: MiddlewareQueryContext[GQLQueryContext, _, _], ctx: Context[GQLQueryContext, _]): BeforeFieldResult[GQLQueryContext, FieldVal] = {
    import com.gu.contentapi.porter.graphql.permissions
    val maybeRestriction = ctx.field.tags.collectFirst { case permissions.Restricted(tier) => tier }

    maybeRestriction match {
      case Some(restriction)=>
        if(ctx.ctx.userTier < restriction) {
          logger.info(s"denying access to ${ctx.field.name} because it is restricted to $restriction and user is ${ctx.ctx.userTier}")
          throw Errors.PermissionDenied("This field is not permitted for your user tier")
        } else {
          continue
        }
      case None=>
        continue
    }
  }

  override def afterField(queryVal: Unit, fieldVal: Unit, value: Any, mctx: MiddlewareQueryContext[GQLQueryContext, _, _], ctx: Context[GQLQueryContext, _]): Option[Any] = {
    if(ctx.ctx.userTier < InternalTier && ctx.field.name=="channels") {
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
}