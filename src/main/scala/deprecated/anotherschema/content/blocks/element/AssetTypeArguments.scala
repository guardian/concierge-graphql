package deprecated.anotherschema.content.blocks.element

import sangria.schema._
import io.circe.optics.JsonPath

@deprecated("you should be using com.gu.contentapi.porter.graphql")
object AssetTypeArguments {
  val MimeType = Argument("mimeType", OptionInputType(StringType), description="Only retrieve assets with this MIME type")

  val AllAssetTypeArguments = MimeType :: Nil

}
