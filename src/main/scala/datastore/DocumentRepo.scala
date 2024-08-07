package datastore

import com.gu.contentapi.porter.model.{Content, Section, Tag}
import com.sksamuel.elastic4s.requests.searches.sort.SortOrder
import deprecated.anotherschema.Edge
import io.circe.Json
import schema.Content
import security.UserTier
import utils.RawResult

import scala.concurrent.Future

trait DocumentRepo {
  def docById(id:String):Future[Edge[Json]]

  def marshalledDocs(queryString: Option[String], queryFields: Option[Seq[String]],
                     atomId: Option[String], forChannel:Option[String], userTier: UserTier,
                     tagIds: Option[Seq[String]], excludeTags: Option[Seq[String]],
                     sectionIds: Option[Seq[String]], excludeSections: Option[Seq[String]],
                     orderDate:Option[String], orderBy:Option[SortOrder],
                     limit: Option[Int], cursor: Option[String]): Future[Edge[Content]]

  def marshalledTags(maybeQuery:Option[String],
                     maybeFuzziness:Option[String],
                     maybeTagId:Option[String],
                     maybeSection: Option[String],
                     tagType:Option[String],
                     maybeCategory:Option[String],
                     maybeReferences:Option[String],
                     orderBy: Option[SortOrder],
                     limit: Option[Int],
                     cursor: Option[String]): Future[Edge[Tag]]

  def tagsForList(tagIdList:Seq[String], maybeSection: Option[String], tagType:Option[String], maybeCategory:Option[String], maybeReferences:Option[String]):Future[Seq[Tag]]

  def atomsForList(atomIds: Seq[String], atomType: Option[String]):Future[Seq[Json]]

  def atoms(atomIds: Option[Seq[String]], queryString: Option[String],
            queryFields: Option[Seq[String]], atomType: Option[String],
            revisionBefore: Option[Long], revisionAfter: Option[Long],
            orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Edge[Json]]

  def sectionForId(sectionId:String):Future[Option[Section]]
}
