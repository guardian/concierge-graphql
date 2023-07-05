package com.gu.contentapi.porter.graphql

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

object DateTime {
  //FIXME - verify the exact proper format for 2015-09-21T17:00:15Z
  val Formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
}
