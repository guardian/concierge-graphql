package com.gu.porter.graphql

import com.gu.contentapi.porter.graphql.DateTime
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import scala.util.{Success, Try}

class DateTimeSpec extends AnyFlatSpec with Matchers {
  it must "decode a known date string" in {
    val result = Try { LocalDateTime.parse("2015-09-21T17:00:15", DateTime.Formatter)}
    result must be(Success(LocalDateTime.of(2015, 9,21, 17,0,15,0)))
  }

  it must "encode a known date" in {
    val result = Try { LocalDateTime.of(2015,9,21,17,0,15,0).format(DateTime.Formatter)}
    result must be(Success("2015-09-21T17:00:15"))
  }
}
