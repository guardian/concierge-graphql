package anotherschema.content.blocks

import anotherschema.content.blocks.element.AssetTypeArguments
import anotherschema.{CirceHelpers, SchemaDefinition}
import io.circe.Json
import sangria.schema._
import io.circe.optics.JsonPath
import io.circe.syntax._

object Element extends SchemaDefinition with CirceHelpers {
  private val ElementTypeX = JsonPath.root.`type`.string

  val ElementTypes = EnumType(
    "ElementType",
    Some("The types of elements available"),
    List(
      EnumValue(
        "text",
        Some("An HTML formatted text block"),
        "text"
      ),
      EnumValue(
        "image",
        Some("An image, usually jpeg"),
        "image"
      ),
      EnumValue(
        "pullquote",
        Some("A relevant quotation to be highlighted"),
        "pullquote"
      )
    )
  )
  val assetTypeData: ObjectType[Unit, Json] = ObjectType(
    "AssetTypeData",
    "Type-specific metadata for assets",
    ()=>fields[Unit, Json](
      Field("aspectRatio",OptionType(StringType), Some("Display aspect ratio if an image or video"), resolve=ctx=>JsonPath.root.aspectRatio.string.getOption(ctx.value)),
      Field("altText", OptionType(StringType), Some("Alternate display text for an image"), resolve=ctx=>JsonPath.root.altText.string.getOption(ctx.value)),
      Field("isInappropriateForAdverts", OptionType(BooleanType), Some("If set don't show ads"), resolve=ctx=>JsonPath.root.isInappropriateForAdverts.boolean.getOption(ctx.value)),
      Field("caption", OptionType(StringType), Some("Caption to describe this asset"), resolve=ctx=>JsonPath.root.caption.string.getOption(ctx.value)),
      Field("credit", OptionType(StringType), Some("Who to credit for the asset"), resolve=ctx=>JsonPath.root.credit.string.getOption(ctx.value)),
      Field("embeddable", OptionType(BooleanType), Some("If set then this can be embedded elsewhere"), resolve=ctx=>JsonPath.root.embeddable.boolean.getOption(ctx.value)),
      Field("photographer", OptionType(StringType), Some("If a photograph, who took it"), resolve=ctx=>JsonPath.root.photographer.string.getOption(ctx.value)),
      Field("source", OptionType(StringType), Some("Where did this asset originate"), resolve=ctx=>JsonPath.root.source.string.getOption(ctx.value)),
      Field("stillImageUrl", OptionType(StringType), Some("URL if this is a static image"), resolve=ctx=>JsonPath.root.stillImageUrl.string.getOption(ctx.value)),
      Field("width", OptionType(LongType), Some("Width of an image etc."), resolve=ctx=>JsonPath.root.width.long.getOption(ctx.value)),
      Field("height", OptionType(LongType), Some("Height of an image etc."), resolve=ctx=>JsonPath.root.height.long.getOption(ctx.value)),
      Field("name", OptionType(StringType), Some("Name of the asset"), resolve=ctx=>JsonPath.root.name.string.getOption(ctx.value)),
      Field("secureFile", OptionType(StringType), Some("SSL URL to the file"), resolve=ctx=>JsonPath.root.secureFile.string.getOption(ctx.value)),
      Field("isMaster", OptionType(BooleanType), Some("Is this the original version of the asset"), resolve=ctx=>JsonPath.root.isMaster.boolean.getOption(ctx.value)),
      Field("sizeInBytes", OptionType(LongType), Some("Size of the asset blob in bytes"), resolve=ctx=>JsonPath.root.sizeInBytes.long.getOption(ctx.value)),
    )
  )

  val assets: ObjectType[Unit, Json] = ObjectType(
    "ElementAssets",
    "Assets associated with a given element",
    ()=>fields[Unit, Json](
      //FIXME - should be enum
      Field("type", StringType, Some("Type of the asset"), resolve=ctx=>JsonPath.root.`type`.string.getOption(ctx.value).get),
      Field("mimeType", OptionType(StringType), Some("MIME type of the asset's data"), resolve=ctx=>JsonPath.root.mimeType.string.getOption(ctx.value)),
      Field("file", OptionType(StringType), Some("File name of the asset"), resolve=ctx=>JsonPath.root.file.string.getOption(ctx.value)),
      Field("typeData", OptionType(assetTypeData), Some("Type-specific metadata"), resolve=ctx=>JsonPath.root.typeData.obj.getOption(ctx.value).map(_.asJson)),
    )
  )

  val textTypeData: ObjectType[Unit, Json] = ObjectType(
    "TextTypeData",
    "Data present if the element is a 'text' type",
    ()=>fields[Unit, Json](
      Field("html", StringType, Some("HTML content of the element"), resolve=ctx=>JsonPath.root.html.string.getOption(ctx.value).get)
    )
  )

  val pullquoteTypeData: ObjectType[Unit, Json] = ObjectType(
    "PullquoteTypeData",
    "Data present if the element is a pullquote",
    () => fields[Unit, Json](
      Field("html", StringType, Some("HTML content of the element"), resolve = ctx => JsonPath.root.html.string.getOption(ctx.value).get),
      Field("attribution", OptionType(StringType), Some("Who the quote is attributed to"), resolve=ctx=>JsonPath.root.attribution.string.getOption(ctx.value))
    )
  )

  override val definition: ObjectType[Unit, Json] = ObjectType(
    "Element",
    "Individual element that makes up an article",
    ()=>fields[Unit, Json](
      Field("type",ElementTypes, Some("type of the element"), resolve=ctx=>ElementTypeX.getOption(ctx.value).get),
      Field("assets",ListType(assets),
        Some("external assets associated with this element"),
        arguments=AssetTypeArguments.AllAssetTypeArguments,
        resolve= ctx=>JsonList(ctx.value \\ "assets")
      ),
      Field("textTypeData", OptionType(textTypeData), Some("Data specific to text elements"), resolve=ctx=>JsonPath.root.textTypeData.obj.getOption(ctx.value).map(_.asJson)),
      Field("pullquoteTypeData", OptionType(pullquoteTypeData), Some("Data specific to pullquote elements"), resolve = ctx => JsonPath.root.pullquoteTypeData.obj.getOption(ctx.value).map(_.asJson))

    )
  )
}
