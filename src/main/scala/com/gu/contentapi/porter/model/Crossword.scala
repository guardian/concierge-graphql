package com.gu.contentapi.porter.model

import io.circe.Codec
import io.circe.generic.semiauto._

case class Crossword(
  name: String,
  `type`: String,
  number: Int,
  date: String, // yyyy-mm-dd
  dimensions: CrosswordDimensions,
  entries: String,
  solutionAvailable: Boolean,
  dateSolutionAvailable: Option[String],
  hasNumbers: Boolean,
  randomCluesOrdering: Boolean,
  instructions: Option[String],
  creator: Option[CrosswordCreator],
  pdf: Option[String],
  annotatedSolution: Option[String])

case class CrosswordDimensions(cols: Int, rows: Int)

case class CrosswordCreator(name: String, webUrl: String)

object Crossword {
  implicit val codecCrosswordCreator: Codec[CrosswordCreator] = deriveCodec
  implicit val codecCrosswordDimensions: Codec[CrosswordDimensions] = deriveCodec
  implicit val codecCrossword: Codec[Crossword] = deriveCodec
}

