package datastore

import com.gu.etagcaching.aws.s3.S3ByteArrayFetching
import com.gu.facia.client.{ApiClient, Environment}
import com.gu.etagcaching.aws.sdkv2.s3.S3ObjectFetching
import com.gu.facia.api.models.{CollectionConfig, Collection => FaciaCollection, Front => FaciaFront}
import deprecated.anotherschema.Edge
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import software.amazon.awssdk.auth.credentials.{AwsCredentialsProviderChain, DefaultCredentialsProvider, ProfileCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

import scala.concurrent.{ExecutionContext, Future}

class FrontsRepo()(implicit ec: ExecutionContext) {
  val provider = AwsCredentialsProviderChain.builder().credentialsProviders(
    ProfileCredentialsProvider.create("cmsFronts"),
    DefaultCredentialsProvider.builder().build()
  ).build()
  val s3Client = S3AsyncClient.builder()
    .credentialsProvider(provider)
    .region(Region.EU_WEST_1)
    .build()
  private val s3Fetching: S3ByteArrayFetching = S3ObjectFetching.byteArraysWith(s3Client)
  private val apiClient: ApiClient = ApiClient.withCaching("facia-tool-store", Environment.Code, s3Fetching)

  def getFronts(frontIds: Option[Seq[String]]): Future[Edge[FaciaFront]] =
    apiClient.config.flatMap { response =>
      val fronts = frontIds.map { ids =>
        response.fronts.filter(front => ids.contains(front._1))
      }.getOrElse(response.fronts).map {
        case (id, frontJson) =>
          FaciaFront.fromFrontJson(id, frontJson)
      }

      Future.successful(Edge(
        fronts.size,
        Some(0.toString),
        false,
        fronts.toList
      ))
    }

  def getCollections(collectionIds: Seq[String]): Future[Seq[FaciaCollection]] = {
    val eventuallyConfig = apiClient.config
    val eventuallyCollections = Future.traverse(collectionIds)(id => apiClient.collection(id).map {
      case Some(col) => Some((id, col))
      case None => None
    })
    for {
      config <- eventuallyConfig
      collections <- eventuallyCollections
    } yield {
      collections.flatten.map {
        case (id, col) =>
          FaciaCollection.fromCollectionJsonConfigAndContent(id, Some(col), CollectionConfig.fromCollectionJson(config.collections(id)))
      }
    }
  }
}
