package datastore

import io.circe.Json
import schema.Content

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Iterable[Json]]
  def docsByWebTitle(webTitle:String):Future[Iterable[Json]]
}
