package datastore

import io.circe.Json
import schema.Content

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Json]
  def docsByWebTitle(webTitle:String):Future[Json]
}
