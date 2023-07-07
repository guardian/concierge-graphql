package middleware

import io.prometheus.client.Histogram

case class FieldMetricState(activeTimer:Option[Histogram.Timer])

object FieldMetricState {
  def apply() = new FieldMetricState(None)
}
