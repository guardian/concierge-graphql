package datastore

import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import io.circe.Json
import schema.Content
import utils.RawResult

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Iterable[Json]]
  def docsByWebTitle(webTitle:String, orderDate:Option[String], orderBy:Option[SortOrder]):Future[Iterable[Json]]
}
