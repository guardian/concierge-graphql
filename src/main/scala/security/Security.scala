package security

import cats.effect.IO
import com.typesafe.config.Config
import org.http4s.{Request, Response}
import org.http4s.dsl.io._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

class Security(private val auth:ApiKeyAuth) extends Authenticator {
  def limitByTier(req:Request[IO], minTier:UserTier)(protectedCb: UserTier => IO[Response[IO]]):IO[Response[IO]] = {
    auth.extractUserTier(req) match {
      case Some(tier)=>
        if(tier < minTier) {
          Forbidden("Currently only internal-tier keys are allowed access")
        } else {
          protectedCb(tier)
        }
      case None=>
        Forbidden("You must have an API key to access this resource")
    }
  }
}

object Security {
  class PermissionDeniedException extends Exception

  val authCacheTtl = 5.minutes
  def apply(config:Config) = {
    val auth = ApiKeyAuth(authCacheTtl, config.getString("aws.auth_table"))
    new Security(auth)
  }
}