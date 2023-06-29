package anotherschema.content.blocks.element

import sangria.schema._
import io.circe.optics.JsonPath
object AssetTypeArguments {
  val MimeType = Argument("mimeType", OptionInputType(StringType), description="Only retrieve assets with this MIME type")

  val AllAssetTypeArguments = MimeType :: Nil

}
