package datastore

import com.gu.contentapi.porter.model.{Content, Tag}
import com.sksamuel.elastic4s.sttp.SttpRequestHttpClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, Response}
import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.{ExistsQuery, NestedQuery, Query, RangeQuery}
import com.sksamuel.elastic4s.requests.searches.queries.compound.BoolQuery
import com.sksamuel.elastic4s.requests.searches.queries.matches.{FieldWithOptionalBoost, MatchAllQuery, MatchQuery, MultiMatchQuery}
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, ScoreSort, Sort, SortOrder}
import deprecated.anotherschema.Edge
import io.circe.Json
import org.slf4j.LoggerFactory
import utils.RawResult
import io.circe.generic.auto._
import security.{InternalTier, UserTier}

import scala.reflect.{ClassTag, classTag}

class ElasticsearchRepo(endpoint:ElasticNodeEndpoint, val defaultPageSize:Int=20) extends DocumentRepo {
  private val logger = LoggerFactory.getLogger(getClass)
  private val client = ElasticClient(SttpRequestHttpClient(endpoint))

  override def docById(id: String): Future[Edge[Json]] = {
    client.execute {
      search("content").query(MatchQuery("id", id)).sortByFieldDesc("webPublicationDate")
    }.flatMap(response=>{
      if(response.isSuccess) {
        response
          .result
          .hits
          .hits
          .headOption
          .map(h=>(h, io.circe.parser.parse(h.sourceAsString))) match {
          case None=>
            Future(
              Edge(
                0,
                None,
                false,
                List[Json]()
              )
            )
          case Some((_, Left(err)))=>
            Future.failed(new RuntimeException(err))
          case Some((hit, Right(json)))=>
            Future(
              Edge(
                response.result.hits.total.value,
                Edge.cursorValue(hit),
                false,
                List(json)
              )
            )
        }
      } else {
        Future.failed(response.error.asException)
      }
    })
  }

  private def defaultingSortParam(orderDate:Option[String], orderBy:Option[SortOrder]): Sort = {
    orderDate match {
      case Some(field)=>FieldSort(field, order = orderBy.getOrElse(SortOrder.DESC))
      case None=>ScoreSort(orderBy.getOrElse(SortOrder.DESC))
    }
  }

  private def handleResponseMultiple[D:io.circe.Decoder](response:Response[SearchResponse], pageSize:Int)(mapper:(Json)=>Option[D])(implicit ct:ClassTag[D]):Future[Edge[D]] = {
    if(response.isSuccess) {
      val allResults = response
        .result
        .hits
        .hits
        .map(RawResult.apply)

      val failures = allResults.collect({case Left(err)=>err})
      if(failures.nonEmpty) {
        logger.error(s"${failures.length} entries failed to parse:")
        failures.foreach(err=>logger.error(s"\t${err.getMessage()}"))
        Future.failed(new RuntimeException(s"${failures.length} entries failed to parse:"))
      } else {
        val success = allResults.collect({case Right(content)=>content})

        Future(
          Edge(
            response.result.hits.total.value,
            success.lastOption.flatMap(rec=>Edge.cursorValue(rec.sort)),
            success.length==pageSize, //FIXME - is there a better a way of determining if there is likely to be more content?
            success.map(_.fulljson).map(mapper).collect({case Some(d)=>d})
          )
        )
      }
    } else {
      Future.failed(response.error.asException)
    }
  }

  private def identityTransform:Json=>Option[Json] = (j:Json)=>Some(j)

  private def contentTransform:Json=>Option[Content] = _.as[Content] match {
    case Right(content)=>Some(content)
    case Left(err)=>
      logger.error(s"Could not convert object from json: ", err)
      None
  }

  override def docsByWebTitle(webTitle: String, orderDate:Option[String], orderBy:Option[SortOrder], limit:Option[Int], cursor:Option[String]): Future[Edge[Json]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    Edge.decodeCursor(cursor) match {
      case Right(maybeCursor) =>
        client.execute {
          search("content")
            .query(MatchQuery("webTitle", webTitle))
            .sortBy(defaultingSortParam(orderDate, orderBy))
            .limit(pageSize)
            .searchAfter(maybeCursor)
        }.flatMap(handleResponseMultiple(_, pageSize)(identityTransform))
      case Left(err) =>
        Future.failed(new RuntimeException(s"Unable to decode cursor value $cursor: $err"))
    }
  }

  private def limitToChannelQuery(channel:String):Query = NestedQuery(
    path="channels",
    query=BoolQuery(must=Seq(
      MatchQuery("channels.channelId", channel),
      MatchQuery("channels.fields.isAvailable", true)
    ))
  )

  override def marshalledDocs(queryString: Option[String], queryFields: Option[Seq[String]],
                              atomId: Option[String], forChannel:Option[String], userTier:UserTier,
                              tagIds: Option[Seq[String]], excludeTags: Option[Seq[String]],
                              sectionIds: Option[Seq[String]], excludeSections: Option[Seq[String]],
                              orderDate: Option[String], orderBy: Option[SortOrder],
                              limit: Option[Int], cursor: Option[String]): Future[Edge[Content]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    val fieldsToQuery = queryFields
      .getOrElse(Seq("webTitle", "path"))  //default behaviour from Concierge
      .map(FieldWithOptionalBoost(_, None))

    //only internal tier users are allowed to query other channels
    val selectedChannel = if(userTier==InternalTier) forChannel.getOrElse("open") else "open"

    val params:Seq[Query] = Seq(
      Some(limitToChannelQuery(selectedChannel)),
      queryString.map(MultiMatchQuery(_, fields = fieldsToQuery)),
      atomId.map(MatchQuery("atomIds.id", _)),
      tagIds.map(tags=>BoolQuery(must=tags.map(MatchQuery("tags", _)))) ,
      excludeTags.map(tags=>BoolQuery(not=Seq(BoolQuery(should=tags.map(MatchQuery("tags", _)))))),
      sectionIds.map(s=>BoolQuery(should=s.map(MatchQuery("sectionId", _)))),
      excludeSections.map(s=>BoolQuery(not=Seq(BoolQuery(should=s.map(MatchQuery("sectionId", _))))))
    ).collect({case Some(q)=>q})

    Edge.decodeCursor(cursor) match {
      case Right(maybeCursor) =>
        client.execute {
          search("content")
            .query(BoolQuery(must=params))
            .sortBy(defaultingSortParam(orderDate, orderBy))
            .limit(pageSize)
            .searchAfter(maybeCursor)
        }.flatMap(handleResponseMultiple(_, pageSize)(contentTransform))
      case Left(err) =>
        Future.failed(new RuntimeException(s"Unable to decode cursor value $cursor: $err"))
    }
  }

  private def tagQueryParams(maybeTagId:Option[String], maybeSection:Option[String], tagType:Option[String]):Seq[Query] = {
    Seq(
      maybeTagId.map(MatchQuery("id", _)),
      maybeSection.map(MatchQuery("sectionId", _)),
      tagType.map({
        case "podcast" =>
          ExistsQuery("podcast")
        case tp: String =>
          MatchQuery("type", tp)
      })
    ).collect({ case Some(param) => param })
  }
  private def buildTagQuery(maybeTagId:Option[String], maybeSection:Option[String], tagType:Option[String]) = {
    val base = search("tag")

    val params = tagQueryParams(maybeTagId, maybeSection, tagType)

    if(params.isEmpty) {
      base
    } else {
      base.query(BoolQuery(must=params))
    }
  }

  def tagTransform(j:Json) = j.as[Tag] match {
    case Right(t)=>Some(t)
    case Left(err)=>
      val tagId = (j \\ "id").headOption.map(_.asString).getOrElse("unknown tag id")
      logger.error(s"Could not unmarshal tag for $tagId: $err")
      None
  }

  //FIXME: tagsForList / marshalledTags could be DRY'd out a bit

  override def marshalledTags(maybeTagId:Option[String], maybeSection: Option[String], tagType:Option[String], orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Edge[Tag]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    val sortParam = if(maybeSection.isDefined || tagType.isDefined) {
      ScoreSort(orderBy.getOrElse(SortOrder.DESC))
    } else {
      FieldSort("id", order = orderBy.getOrElse(SortOrder.ASC))
    }

    Edge.decodeCursor(cursor) match {
      case Right(maybeCursor)=>
        client.execute {
          buildTagQuery(maybeTagId, maybeSection, tagType)
            .sortBy(sortParam)
            .limit(pageSize)
            .searchAfter(maybeCursor)
        }.flatMap(handleResponseMultiple(_, pageSize)(tagTransform))
      case Left(err)=>
        Future.failed(new RuntimeException(s"Unable to decode cursor value $cursor: $err"))
    }
  }

  override def tagsForList(tagIdList:Seq[String], maybeSection: Option[String], tagType:Option[String]):Future[Seq[Tag]] = {
    val tagIdMatches = tagIdList.map(MatchQuery("id", _))

    client.execute {
      val restrictions = tagQueryParams(None, maybeSection, tagType)

      if(restrictions.nonEmpty) {
        search("tag").query(
            BoolQuery(
              must=restrictions :+ BoolQuery(should=tagIdMatches)
            )
          )
      } else {
        search("tag").query(BoolQuery(should=tagIdMatches))
      }

    } flatMap { response=>
      if(response.isError) {
        logger.error(s"Could not make query for tags ${tagIdList}: ${response.error}")
        Future.failed(response.error.asException)
      } else {
        Future(response.result.hits.hits.map(hit=>
          for {
            rawResult <- RawResult(hit)
            marshalledResult <- rawResult.content.as[Tag]
          } yield marshalledResult
        ))
          .map(_.collect({case Right(tag)=>tag}))
          .map(_.toSeq)
      }
    }
  }

  private def findAtoms(atomIds: Option[Seq[String]], queryString: Option[String],
                        queryFields: Option[Seq[String]], atomType: Option[String],
                        revisionBefore: Option[Long], revisionAfter: Option[Long],
                        orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Response[SearchResponse]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    val sortParam = if (queryString.isDefined || atomIds.isDefined) {
      ScoreSort(orderBy.getOrElse(SortOrder.DESC))
    } else {
      //TODO: should offer a choice of sort field
      FieldSort("contentChangeDetails.lastModified.date", order = orderBy.getOrElse(SortOrder.DESC))
    }

    val fieldsToQuery = queryFields
      .getOrElse(Seq("title", "labels"))
      .map(FieldWithOptionalBoost(_, None))

    val revisionRange = (revisionBefore, revisionAfter) match {
      case (None, None) =>
        None
      case _ =>
        Some(RangeQuery("contentChangeDetails.revision", gt = revisionAfter, lt = revisionBefore))
    }

    val params = Seq(
      atomIds.map(idList => BoolQuery(should = idList.map(MatchQuery("id", _)))),
      queryString.map(MultiMatchQuery(_, fields = fieldsToQuery)),
      atomType.map(MatchQuery("atomType", _)),
      revisionRange,
    ).collect({ case Some(param) => param })

    Edge.decodeCursor(cursor) match {
      case Right(maybeCursor) =>
        client.execute {
          val q = if (params.isEmpty) {
            MatchAllQuery()
          } else {
            BoolQuery(must = params)
          }

          search("atoms").query(q).sortBy(sortParam)
            .limit(pageSize)
            .searchAfter(maybeCursor)
        }
      case Left(err) =>
        Future.failed(new RuntimeException(s"Unable to decode cursor value $cursor: $err"))
    }
  }

  /**
   * Queries for a defined list of atoms without pagination, intended for lifting a list of atoms to hydrate from an article
   * @param atomIds
   * @param atomType
   * @return
   */
  override def atomsForList(atomIds: Seq[String], atomType: Option[String]) = {
    findAtoms(Some(atomIds), None, None, atomType, None,None, None, Some(atomIds.length), None) flatMap { response =>
      if (response.isError) {
        logger.error(s"Could not make query for tags $atomIds: ${response.error}")
        Future.failed(response.error.asException)
      } else {
        Future(response.result.hits.hits
          .map(RawResult.apply)
          .map(_.map(_.content))
          .collect({ case Right(tag) => tag })
        )
          .map(_.toSeq)
      }
    }
  }

  override def atoms(atomIds: Option[Seq[String]], queryString: Option[String],
                     queryFields: Option[Seq[String]], atomType: Option[String],
                     revisionBefore: Option[Long], revisionAfter: Option[Long],
                     orderBy: Option[SortOrder], limit: Option[Int], cursor: Option[String]): Future[Edge[Json]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    findAtoms(atomIds, queryString, queryFields, atomType, revisionBefore, revisionAfter, orderBy, limit, cursor).flatMap(handleResponseMultiple(_, pageSize)(identityTransform))
  }
}
