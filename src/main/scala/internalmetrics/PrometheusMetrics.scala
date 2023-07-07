package internalmetrics

import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.{CollectorRegistry, Counter, Histogram}

import java.io.StringWriter

object PrometheusMetrics {
  val QueryTime = Histogram
    .build("concierge_graphql_query_time", "histogram measuring the time taken for query execution")
    .register()

  val FieldUsageCount = Counter
    .build("concierge_graphql_field_usage","count the usages of different fields")
    .labelNames("field_name")
    .register()

  val FieldErrorCount = Counter
    .build("concierge_graphql_field_error", "count the number of times that processing has failed on a given field")
    .labelNames("field_name")
    .register()

  def dumpMetrics:String = {
    val writer = new StringWriter()
    TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
    writer.toString
  }
}
