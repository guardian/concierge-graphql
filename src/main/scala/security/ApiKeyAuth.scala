package security

import cats.effect.IO
import org.http4s.Request
import org.slf4j.LoggerFactory
import org.typelevel.ci.CIString
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider, ProfileCredentialsProvider, SystemPropertyCredentialsProvider}
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest}
import utils.AWSUtils
import java.time.{Duration, Instant}
import java.util.{Timer, TimerTask}
import scala.collection.SortedSet
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

case class CacheTrackingEntry(key:String, time:Instant)

object CacheTrackingEntry {
  implicit val ordering:Ordering[CacheTrackingEntry] =
    (x: CacheTrackingEntry, y: CacheTrackingEntry) => if ((x.time.toEpochMilli - y.time.toEpochMilli) < 0) {
      -1
    } else if (x.time.toEpochMilli == y.time.toEpochMilli) {
      0
    } else {
      1
    }
}

class ApiKeyAuth(dynamoDbClient:DynamoDbClient, tableName:String, cachingTtl:FiniteDuration) {
  private val logger = LoggerFactory.getLogger(getClass)
  logger.info(s"AWS dynamodb auth initialised.  TableName is $tableName")

  private var localCache:Map[String,UserTier] = Map()

  private var cacheTracking:SortedSet[CacheTrackingEntry] = SortedSet()(CacheTrackingEntry.ordering)

  private val timer = new Timer()

  timer.schedule(new TimerTask {
    override def run(): Unit = {
      logger.debug("Cleanup timer running")
      val cutoff = Instant.now().minus(cachingTtl.length, cachingTtl.unit.toChronoUnit)
      this.synchronized {
        cacheTracking = cacheTracking.filter(e=>e.time.isAfter(cutoff))
      }
    }
  }, 1000L, 1000L)

  def extractUserTier(req:Request[IO]):Option[UserTier] = {
    req.headers
      .get(ApiKeyAuth.name)
      .flatMap(keyValue=>{
        lookUpInCache(keyValue.head.value) match {
          case Some(tier)=>Some(tier)
          case None=>
            for {
              tierName <- lookUpKey(keyValue.head.value)
              tier <- UserTier(tierName)
              _ = updateCache(keyValue.head.value, tier)
            } yield tier
        }
      })
  }

  private def lookUpInCache(keyValue:String):Option[UserTier] = this.synchronized {
    localCache.get(keyValue)
  }

  private def updateCache(keyValue:String, userTier:UserTier):Unit = this.synchronized {
    localCache = localCache + (keyValue -> userTier)
    cacheTracking = cacheTracking + CacheTrackingEntry(keyValue, Instant.now())
  }

  /**
   * Internal method to look up a key in the backing store
   * @param keyValue
   * @return
   */
  private def lookUpKey(keyValue:String):Option[String] = {
    try {
      val response = dynamoDbClient.getItem(GetItemRequest.builder()
        .tableName(tableName)
        .key(Map("ApiKey"->AttributeValue.fromS(keyValue)).asJava)
        .build()
      )

      for {
        maybeItem <- Option(response.item())
        itemAsScala = maybeItem.asScala
        attributeValue <- itemAsScala.get("UserTier")
        tier = attributeValue.s()
      } yield tier
    } catch {
      case err:Throwable=>
        logger.error(s"Unable to verify API key ${keyValue}: ${err.getMessage}", err)
        None
    }
  }
}

object ApiKeyAuth {
  val name = CIString("X-Api-Key")

  def apply(cachingTtl: FiniteDuration, authTable: String): ApiKeyAuth = {
    val ddbClient = DynamoDbClient.builder().credentialsProvider(AWSUtils.credsProvider).build()
    new ApiKeyAuth(ddbClient, authTable, cachingTtl)
  }
}