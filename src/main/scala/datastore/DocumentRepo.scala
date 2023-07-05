package datastore

import anotherschema.Edge
import com.gu.contentapi.porter.model.Content
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import io.circe.Json
import schema.Content
import utils.RawResult

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Edge[Json]]
  def docsByWebTitle(webTitle:String, orderDate:Option[String], orderBy:Option[SortOrder], limit:Option[Int], cursor:Option[String]):Future[Edge[Json]]

  def marshalledDocsByWebTitle(webTitle: String, orderDate: Option[String], orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Edge[Content]]

}
