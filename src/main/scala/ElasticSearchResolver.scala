import com.sksamuel.elastic4s.ElasticNodeEndpoint

object ElasticSearchResolver {
  private def local = Option(ElasticNodeEndpoint("http","localhost",9200, None))
  private def fromEnvironment = {
    Option(System.getenv("ELASTICSEARCH_HOST")).map(host=>{
      val proto = if(System.getenv("ELASTICSEARCH_HTTPS")==null) "http" else "https"
      val port = Option(System.getenv("ELASTICSEARCH_PORT")).map(_.toInt).getOrElse(9200)
      ElasticNodeEndpoint(proto, host, port, Option(System.getenv("ELASTICSEARCH_PREFIX")))
    })
  }

  def resolve():ElasticNodeEndpoint = {
    fromEnvironment orElse local
  }.get
}
