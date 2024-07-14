package datastore

import com.gu.contentapi.porter.model.{Content, Section, Tag}
import com.sksamuel.elastic4s.sttp.SttpRequestHttpClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, Response}
import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.requests.searches.queries.{DisMaxQuery, ExistsQuery, Fuzzy, FuzzyQuery, NestedQuery, Query, RangeQuery}
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
      search("content")
        .query(
          BoolQuery(must=Seq(
            MatchQuery("id", id),
            MatchQuery("isGone", false),
            MatchQuery("isExpired", false),
          ))
        )
        .sortByFieldDesc("webPublicationDate")
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

  /**
   * handleResponseMultiple is responsible for unmarshalling content and metadata from an elastic4s Response object
   * and returning it as an Edge object.
   * The hits data is unmarshalled into a sequence of the generic type D, by mapping over the `mapper` function which should
   * convert the circe json into an object of type D and return None if it fails.
   * If the response fails, then a failure is returned
   * @param response The response to marshal
   * @param pageSize the requested page size
   * @param mapper a function that takes a Json representation of a single item and marshals it into the required output type.
   *               It must return an Option, if it fails then None should be returned and the result will be dropped from the final result
   * @param ct implicitly provided ClassTag for marshalling the data. The compiler fails without this, but the param is automatically
   *           supplied for case classes
   * @tparam D the data type that each hit will be marshalled into
   * @return a Future, containing a populated Edge including all pagination parameters and the content as a list
   */
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

  private def limitToChannelQuery(channel:String):Query = NestedQuery(
    path="channels",
    query=BoolQuery(must=Seq(
      MatchQuery("channels.channelId", channel),
      MatchQuery("channels.fields.isAvailable", true)
    ))
  )

  private val standardAvailabilityQuery:Seq[Query] = Seq(
    MatchQuery("isExpired", false),
    MatchQuery("isGone", false),
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

    val params:Seq[Query] = standardAvailabilityQuery ++ Seq(
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

  private def tagQueryParams(maybeTagId:Option[String], maybeSection:Option[String],
                             tagType:Option[String], maybeCategory:Option[String],
                             maybeReferences: Option[String], queryString:Option[String], fuzziness:Option[String]):Seq[Query] = {
    Seq(
      queryString.map(qs=>{
        if(fuzziness.getOrElse("AUTO") != "OFF") {
          //Why DisMax here? Because we want to include exact-matches as well, if they are relevant. E.g. FuzzyQuery on "politics" returns no results!
          DisMaxQuery(Seq(
            FuzzyQuery("webTitle", qs, fuzziness),
            MatchQuery("webTitle", qs)
          ))
        } else {
          MatchQuery("webTitle", qs)
        }
      }),
      maybeTagId.map(MatchQuery("id", _)),
      maybeSection.map(MatchQuery("sectionId", _)),
      tagType.map({
        case "podcast" =>
          ExistsQuery("podcast")
        case tp: String =>
          MatchQuery("type", tp)
      }),
      maybeSection.map(s=>termQuery("section",s)),
      maybeCategory.map(cat=>termQuery("tagCategories", cat)),
      maybeReferences.map(ref=>termQuery("references", ref))  //this is an object field - check how terming works!!
    ).collect({ case Some(param) => param })
  }

  private def buildTagQuery(maybeTagId:Option[String],
                            maybeSection:Option[String],
                            tagType:Option[String], maybeQuery:Option[String],
                            maybeFuzziness:Option[String],
                            maybeCategory:Option[String], maybeReferences:Option[String]) = {
    val baseSearch = search("tag")

    val params = tagQueryParams(maybeTagId, maybeSection, tagType, maybeCategory, maybeReferences, maybeQuery, maybeFuzziness)

    if(params.isEmpty) {
      baseSearch
    } else {
      baseSearch.query(BoolQuery(must=params))
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

  override def marshalledTags(maybeQuery:Option[String],
                              maybeFuzziness:Option[String],
                              maybeTagId:Option[String],
                              maybeSection: Option[String],
                              tagType:Option[String],
                              maybeCategory:Option[String],
                              maybeReferences:Option[String],
                              orderBy: Option[SortOrder],
                              limit: Option[Int],
                              cursor: Option[String]): Future[Edge[Tag]] = {
    val pageSize = limit.getOrElse(defaultPageSize)

    val sortParam = if(maybeSection.isDefined || tagType.isDefined) {
      ScoreSort(orderBy.getOrElse(SortOrder.DESC))
    } else {
      FieldSort("id", order = orderBy.getOrElse(SortOrder.ASC))
    }

    Edge.decodeCursor(cursor) match {
      case Right(maybeCursor)=>
        client.execute {
          buildTagQuery(maybeTagId, maybeSection, tagType, maybeQuery, maybeFuzziness, maybeCategory, maybeReferences)
            .sortBy(sortParam)
            .limit(pageSize)
            .searchAfter(maybeCursor)
        }.flatMap(handleResponseMultiple(_, pageSize)(tagTransform))
      case Left(err)=>
        Future.failed(new RuntimeException(s"Unable to decode cursor value $cursor: $err"))
    }
  }

  private def marshalTags(response:Future[Response[SearchResponse]]):Future[Seq[Tag]] = {
    response flatMap { response=>
      if(response.isError) {
        logger.error(s"Could not make query for tags: ${response.error}")
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

  override def tagsForList(tagIdList:Seq[String], maybeSection: Option[String], tagType:Option[String], maybeCategory:Option[String], maybeReferences:Option[String]):Future[Seq[Tag]] = {
    val tagIdMatches = tagIdList.map(MatchQuery("id", _))

    val response = client.execute {
      val restrictions = tagQueryParams(None, maybeSection, tagType, maybeCategory, maybeReferences, None, None)

      if(restrictions.nonEmpty) {
        search("tag").query(
            BoolQuery(
              must=restrictions :+ BoolQuery(should=tagIdMatches)
            )
          )
      } else {
        search("tag").query(BoolQuery(should=tagIdMatches))
      }
    }
    marshalTags(response)
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

  override def sectionForId(sectionId:String):Future[Option[Section]] = {
    client.execute {
      search("section")
        .query(MatchQuery("id", sectionId))
    }.flatMap(response=>{
      if(response.isError) {
        logger.error(s"Unable to query Elasticsearch for section $sectionId: ${response.error.toString}")
        Future.failed(response.error.asException)
      } else {
        Future(
          for {
            hit <- response.result.hits.hits.headOption
            raw <- RawResult(hit) match {
              case Right(r)=>Some(r)
              case Left(err)=>
                logger.error(s"$err")
                None
            }
            section <- raw.content.as[Section] match {
              case Right(s)=>Some(s)
              case Left(err)=>
                logger.error(s"$err")
                None
            }
          } yield section
        )
      }
    })
  }
}
