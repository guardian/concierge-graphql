package middleware

import com.fasterxml.jackson.module.scala.deser.overrides.TrieMap
import io.prometheus.client.Histogram
import internalmetrics.PrometheusMetrics
import org.slf4j.LoggerFactory
import sangria.execution.{BeforeFieldResult, Middleware, MiddlewareAfterField, MiddlewareErrorField, MiddlewareQueryContext}
import sangria.schema.Context

import scala.collection.concurrent.TrieMap

class FieldMetrics extends Middleware[Any] with MiddlewareAfterField[Any] with MiddlewareErrorField[Any] {
  private val logger = LoggerFactory.getLogger(getClass)

  type QueryVal = FieldMetricState
  type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[Any, _, _]): QueryVal = {
    new middleware.FieldMetricState(Some(PrometheusMetrics.QueryTime.startTimer()))

  }

  override def afterQuery(queryVal: FieldMetricState, context: MiddlewareQueryContext[Any, _, _]): Unit = {
    queryVal.activeTimer.map(_.observeDuration())
  }

  override def beforeField(queryVal: FieldMetricState, mctx: MiddlewareQueryContext[Any, _, _], ctx: Context[Any, _]): BeforeFieldResult[Any, Unit] = continue

  override def afterField(queryVal: FieldMetricState,
                          fieldVal: Unit, value: Any,
                          mctx: MiddlewareQueryContext[Any, _, _], ctx: Context[Any, _]): Option[Any] = {
    val fieldName = ctx.parentType.name + "." + ctx.field.name
    PrometheusMetrics.FieldUsageCount.labels(fieldName).inc()
    None
  }

  override def fieldError(queryVal: FieldMetricState, fieldVal: Unit, error: Throwable, mctx: MiddlewareQueryContext[Any, _, _], ctx: Context[Any, _]): Unit = {
    //Interesting - could punt something to Sentry or whatever here
    val fieldName = ctx.parentType.name + "." + ctx.field.name
    logger.warn(s"An error was caught processing field $fieldName on path ${ctx.path.toString()}: ${error.getMessage}")
    PrometheusMetrics.FieldErrorCount.labels(fieldName).inc()
  }
}

object FieldMetrics {
  def singleton = new FieldMetrics()
}