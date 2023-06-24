package datastore

import anotherschema.Edge
import com.sksamuel.elastic4s.sttp.SttpRequestHttpClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties}
import com.sksamuel.elastic4s.ElasticDsl._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchQuery
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, ScoreSort, Sort, SortOrder}
import io.circe.{Json, ParsingFailure}
import io.circe.parser._
import io.circe.syntax._
import org.slf4j.LoggerFactory
import utils.RawResult

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

  override def docsByWebTitle(webTitle: String, orderDate:Option[String], orderBy:Option[SortOrder]): Future[Edge[Json]] = {
    client.execute {
      search("content")
        .query(MatchQuery("webTitle", webTitle))
        .sortBy(defaultingSortParam(orderDate, orderBy))
    }.flatMap(response=>{
      if(response.isSuccess) {
        logger.debug(s"webTitle query $webTitle returned ${response.result.hits} results")
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
              success.lastOption.flatMap(rec=>Edge.cursorValue(rec.sort)),  //FIXME - pagination not actually implemented here!
              success.length==defaultPageSize, //FIXME - is there a better a way of determining if there is likely to be more content?
              success.map(hit=>{
                Json.fromFloat(hit.score) match {
                  case Some(jsScore)=>
                    hit.content.asObject.map(_.+: ("score"->jsScore)).asJson
                  case None=>
                    hit.content
                }
              })
            )
          )
        }
      } else {
        Future.failed(response.error.asException)
      }
    })
  }
}
