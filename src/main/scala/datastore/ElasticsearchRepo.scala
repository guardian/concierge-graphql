package datastore

import com.sksamuel.elastic4s.sttp.SttpRequestHttpClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties}
import schema.Content
import com.sksamuel.elastic4s.ElasticDsl._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.requests.searches.queries.matches.MatchQuery
import io.circe.{Json, ParsingFailure}
import io.circe.parser._
import io.circe.syntax._
import org.slf4j.LoggerFactory

class ElasticsearchRepo(endpoint:ElasticNodeEndpoint) extends DocumentRepo {
  private val logger = LoggerFactory.getLogger(getClass)
  private val client = ElasticClient(SttpRequestHttpClient(endpoint))

  override def docById(id: String): Future[Iterable[Json]] = {
    client.execute {
      search("content").query(MatchQuery("id", id)).sortByFieldDesc("webPublicationDate")
    }.flatMap(response=>{
      if(response.isSuccess) {
        response
          .result
          .hits
          .hits
          .headOption
          .map(_.sourceAsString)
          .map(io.circe.parser.parse) match {
          case None=>
            Future(Seq[Json]())
          case Some(Left(err))=>
            Future.failed(new RuntimeException(err))
          case Some(Right(json))=>
            Future(Seq(json))
        }
      } else {
        Future.failed(response.error.asException)
      }
    })
  }

  override def docsByWebTitle(webTitle: String): Future[Iterable[Json]] = {
    client.execute {
      search("content").query(MatchQuery("webTitle", webTitle)).sortByFieldDesc("webPublicationDate")
    }.flatMap(response=>{
      if(response.isSuccess) {
        logger.debug(s"webTitle query $webTitle returned ${response.result.hits} results")
        val allResults = response
          .result
          .hits
          .hits
          .map(_.sourceAsString)
          .map(io.circe.parser.parse)

        val failures = allResults.collect({case Left(err)=>err})
        if(failures.nonEmpty) {
          logger.error(s"${failures.length} entries failed to parse:")
          failures.foreach(err=>logger.error(s"\t${err.getMessage()}"))
          Future.failed(new RuntimeException(s"${failures.length} entries failed to parse:"))
        } else {
          Future(
            allResults
            .collect({case Right(content)=>content})
          )
        }
      } else {
        Future.failed(response.error.asException)
      }
    })
  }
}
