package anotherschema

import io.circe.Json
import sangria.schema.ObjectType

trait SchemaDefinition {
  import io.circe.optics.JsonPath._
  val definition:ObjectType[Unit, Json]
}
