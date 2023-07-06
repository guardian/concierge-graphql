package deprecated.anotherschema

import io.circe.Json
import sangria.schema.ObjectType
@deprecated("you should be using com.gu.contentapi.porter.graphql")
trait SchemaDefinition {
  import io.circe.optics.JsonPath._
  val definition:ObjectType[Unit, Json]
}
