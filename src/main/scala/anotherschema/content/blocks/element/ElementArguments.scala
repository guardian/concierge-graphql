package anotherschema.content.blocks.element

import io.circe.Json
import io.circe.optics.JsonPath
import sangria.schema._
import anotherschema.content.blocks.Element
object ElementArguments {
  val ElementType = Argument("type", OptionInputType(Element.ElementTypes), description = "Only retrieve assets with this element type")

  val AllElementArguments = ElementType :: Nil

  def filterByType(source:Seq[Json], maybeType:Option[String]):Seq[Json] = {
    val xtract = JsonPath.root.`type`.string

    maybeType match {
      case None=> source
      case Some(filterFor)=>
        source.filter(elem=>xtract.getOption(elem).contains(filterFor))
    }
  }
}
