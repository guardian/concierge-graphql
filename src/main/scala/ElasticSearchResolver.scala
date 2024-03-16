import com.sksamuel.elastic4s.ElasticNodeEndpoint
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import java.net.URL
import scala.util.Try

object ElasticSearchResolver {
  private val logger = LoggerFactory.getLogger(getClass)

  private def local = {
    logger.info("No elasticsearch URL found, defaulting to localhost")
    Option(ElasticNodeEndpoint("http","localhost",9200, None))
  }

  private def fromEnvironment = {
    logger.info("Trying to find elasticsearch URL from environment vars")
    Option(System.getenv("ELASTICSEARCH_HOST")).map(host=>{
      val proto = if(System.getenv("ELASTICSEARCH_HTTPS")==null) "http" else "https"
      val port = Option(System.getenv("ELASTICSEARCH_PORT")).map(_.toInt).getOrElse(9200)
      ElasticNodeEndpoint(proto, host, port, Option(System.getenv("ELASTICSEARCH_PREFIX")))
    })
  }

  private def fromConfig(config:Config) = {
    logger.info("Trying to find elasticsearch URL from config...")
    Try {
      val urlString = config.getString("elasticsearch.url")
      val url = new URL(urlString)
      val result = ElasticNodeEndpoint(url.getProtocol, url.getHost, url.getPort, None)
      logger.info(s"Got Elasticsearch URL ${url.toString} from config")
      result
    }.toOption
  }

  def resolve(config:Config):ElasticNodeEndpoint = {
    fromConfig(config) orElse fromEnvironment orElse local
  }.get
}
