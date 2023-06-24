package datastore

import anotherschema.Edge
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import io.circe.Json
import schema.Content
import utils.RawResult

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Edge[Json]]
  def docsByWebTitle(webTitle:String, orderDate:Option[String], orderBy:Option[SortOrder]):Future[Edge[Json]]
}
