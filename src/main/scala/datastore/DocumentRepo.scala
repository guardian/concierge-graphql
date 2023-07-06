package datastore

import com.gu.contentapi.porter.model.{Content, Tag}
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import deprecated.anotherschema.Edge
import io.circe.Json
import schema.Content
import utils.RawResult

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Edge[Json]]
  def docsByWebTitle(webTitle:String, orderDate:Option[String], orderBy:Option[SortOrder], limit:Option[Int], cursor:Option[String]):Future[Edge[Json]]

  def marshalledDocs(queryString: Option[String], queryFields: Option[Seq[String]],
                     tagIds: Option[Seq[String]], excludeTags: Option[Seq[String]],
                     sectionIds: Option[Seq[String]], excludeSections: Option[Seq[String]],
                     orderDate:Option[String], orderBy:Option[SortOrder],
                     limit: Option[Int], cursor: Option[String]): Future[Edge[Content]]

  def marshalledTags(maybeTagId:Option[String], maybeSection: Option[String], tagType:Option[String], orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Edge[Tag]]

  def tagsForList(tagIdList:Seq[String], maybeSection: Option[String], tagType:Option[String]):Future[Seq[Tag]]
}
