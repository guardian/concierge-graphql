package anotherschema

import io.circe.Json
import sangria.schema.Context

trait CirceHelpers {
  def getStringList(ctx: Context[Unit, Json], key: String) = (ctx.value \\ key).flatMap(_.asString.toList)

  def getString(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asString)
  def getStringOrThrow(ctx: Context[Unit, Json], key:String) = getString(ctx, key).get

  def getLong(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asNumber).flatMap(_.toLong)
  def getInt(ctx: Context[Unit, Json], key:String) = (ctx.value \\ key).headOption.flatMap(_.asNumber).flatMap(_.toInt)

  def resolveStringList(key:String) = (ctx:Context[Unit,Json]) => (ctx.value \\ key).flatMap(_.asString.toList)
  def resolveString(key:String) = (ctx:Context[Unit,Json]) => (ctx.value \\ key).headOption.flatMap(_.asString)
}
