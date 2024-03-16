package security

import cats.effect.IO
import org.http4s._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.{AttributeValue, GetItemRequest, GetItemResponse}

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class ApiKeyAuthSpec extends AnyFlatSpec with Matchers with MockitoSugar {
  "APIKeyAuth.extractUserTier" should "look up the incoming key in Dynamo just once, then cache it" in {
    val mockClient = mock[DynamoDbClient]
    when(mockClient.getItem(any[GetItemRequest])).thenReturn(GetItemResponse.builder()
      .item(Map("UserTier" -> AttributeValue.fromS("internal")).asJava)
      .build()
    )

    val toTest = new ApiKeyAuth(mockClient, "", 5.seconds)
    val req = Request[IO](method=Method.POST,headers = Headers("X-Api-Key"->"some-key"))
    val result = toTest.extractUserTier(req)
    result shouldEqual Some(InternalTier)

    val secondResult = toTest.extractUserTier(req)
    secondResult shouldEqual Some(InternalTier)

    verify(mockClient, times(1)).getItem(GetItemRequest.builder()
      .tableName("")
      .key(Map("ApiKey"->AttributeValue.fromS("some-key")).asJava)
      .build()
    )
  }

  "APIKeyAuth.extractUserTier" should "return None if the given key does not exist" in {
    val mockClient = mock[DynamoDbClient]
    when(mockClient.getItem(any[GetItemRequest])).thenReturn(GetItemResponse.builder().build())

    val toTest = new ApiKeyAuth(mockClient, "", 5.seconds)
    val req = Request[IO](method=Method.POST,headers = Headers("X-Api-Key"->"some-key"))
    val result = toTest.extractUserTier(req)
    result shouldEqual None

    val secondResult = toTest.extractUserTier(req)
    secondResult shouldEqual None

    verify(mockClient, times(2)).getItem(GetItemRequest.builder()
      .tableName("")
      .key(Map("ApiKey"->AttributeValue.fromS("some-key")).asJava)
      .build()
    )
  }
}
