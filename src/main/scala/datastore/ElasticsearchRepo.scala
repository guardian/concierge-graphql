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

class ElasticsearchRepo(endpoint:ElasticNodeEndpoint) extends DocumentRepo {

  private val client = ElasticClient(SttpRequestHttpClient(endpoint))

  override def docById(id: String): Future[Json] = {
    client.execute {
      search("content").query(MatchQuery("id", id))
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
            Future(Json.obj())
          case Some(Left(err))=>
            Future.failed(new RuntimeException(err))
          case Some(Right(json))=>
            Future(json)
        }
      } else {
        Future.failed(response.error.asException)
      }
    })
  }
}
