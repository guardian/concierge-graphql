package com.gu.contentapi.porter.graphql

import com.gu.contentapi.porter.model.Content
import org.slf4j.LoggerFactory
import sangria.schema.{Action, Argument, Context, EnumType, EnumValue, ListInputType, OptionInputType}

import scala.util.{Success, Try}

object AlternateIdParameters {
  private val logger = LoggerFactory.getLogger(getClass)

  val AlternateIdTypes = EnumType(
    "AlternateIdType",
    Some("Different types of alternate IDs available"),
    List(
      EnumValue("all",
        value="all",
        description=Some("Return all alternate IDs"),
      ),
      EnumValue("url",
        value="urlpath",
        description=Some("The full URL reference")
      ),
      EnumValue("shortcode",
        value="shortcode",
        description = Some("URL short-code")
      ),
      EnumValue("internalComposer",
        value = "composer",
        description = Some("Internal composer code reference")
      ),
      EnumValue("internalPage",
        value = "page",
        description = Some("Internal page code reference")
      ),
      EnumValue("internalOctopus",
        value = "octopus",
        description = Some("Internal print system reference")),
      EnumValue("internalPluto",
        value = "pluto",
        description = Some("Internal video production system reference"))
    )
  )
  object AlternateId extends Enumeration {
    val all, shortcode, composer, page, urlpath, octopus, pluto = Value
  }

  val ParameterTypeId = Argument(
    "type",
    OptionInputType(ListInputType(AlternateIdTypes)),
    description = "Which types of IDs do you want"
  )

  val AllAlternateIdParameters = ParameterTypeId :: Nil

  private val xtractor = "^([\\w-]+)/(\\w*)".r.unanchored

  private def categoriseIdList(allIdList:Seq[String]):Map[AlternateId.Value, String] =
    allIdList.foldLeft(Map[AlternateId.Value, String]())((map, elem)=>{
      elem match {
        case xtractor(lead, trail)=>
          lead match {
            case "p"=>
              map + (AlternateId.shortcode -> elem)
            case "internal-code"=>
              trail match {
                case "composer"=>
                  map + (AlternateId.composer -> elem)
                case "page"=>
                  map + (AlternateId.page -> elem)
                case "octopus"=>
                  map + (AlternateId.octopus -> elem)
                case "pluto"=>
                  map + (AlternateId.pluto -> elem)
                case _=>
                  logger.warn(s"Unrecognised internal code type: $trail")
                  map
              }
            case _=>
              map + (AlternateId.urlpath -> elem)
          }
        case _=>
          map + (AlternateId.urlpath -> elem)
      }
    })

  def filterIds(allIdList:Seq[String], selectedTypes:Option[Seq[String]]):Seq[String] = selectedTypes match {
    case None => //the default, return everything. For performance, short-circuit unless we were asked to do something
      allIdList
    case Some(rawTypes) =>
      val categorisedIdList = categoriseIdList(allIdList)
      val typesToSelect = rawTypes
        .map(v => Try {
          AlternateId.withName(v)
        })
        .collect({ case Success(v) => v })
        .toSet

      if (typesToSelect.contains(AlternateId.all)) {
        allIdList
      } else {
        typesToSelect
          .map(categorisedIdList.get)
          .collect({ case Some(id) => id })
          .toSeq
      }
  }

  def Resolver[Ctx](ctx:Context[Ctx, Content]) = {
    filterIds(ctx.value.alternateIds, ctx arg ParameterTypeId)
  }
}
