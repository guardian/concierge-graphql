package anotherschema.query


import sangria.schema.{EnumType, EnumValue, ObjectType}
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder

object OrderBy {
  val definition = EnumType(
    "Ordering",
    Some("How to sort the results"),
    List(
      EnumValue("newest",None,SortOrder.DESC),
      EnumValue("oldest", None, SortOrder.ASC)
    )
  )
}

