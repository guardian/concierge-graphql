package anotherschema

import io.circe.Json
import sangria.schema.Context

trait CirceHelpers {
  def getStringList(ctx: Context[Unit, Json], key: String) = (ctx.value \\ key).flatMap(_.asString.toList)

  def getString(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asString)
  def getStringOrThrow(ctx: Context[Unit, Json], key:String) = getString(ctx, key).get

  def getLong(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asNumber).flatMap(_.toLong)
  def getInt(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asNumber).flatMap(_.toInt)

  def getBool(ctx:Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asBoolean)

  /**
   * Takes a list of json which may contain one or more array and flattens it into a single array which contains no nested
   * arrays
   * @param from
   * @return
   */
  def JsonList(from:Seq[Json]):Seq[Json] = from.flatMap(js=>{
    js.asArray match {
      case Some(components)=>
        components
      case None=>
        List(js)
    }
  })

  def resolveStringList(key:String) = (ctx:Context[Unit,Json]) => (ctx.value \\ key).flatMap(_.asString.toList)
  def resolveString(key:String) = (ctx:Context[Unit,Json]) => (ctx.value \\ key).headOption.flatMap(_.asString)
}
