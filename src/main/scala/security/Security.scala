package security

import cats.effect.IO
import org.http4s.{Request, Response}
import org.http4s.dsl.io._

object Security {
  class PermissionDeniedException extends Exception

  def limitByTier(req:Request[IO], minTier:UserTier)(protectedCb: => IO[Response[IO]]):IO[Response[IO]] = {
    KongHeader.extractUserTier(req) match {
      case Some(tier)=>
        if(tier < minTier) {
          Forbidden("Currently only internal-tier keys are allowed access")
        } else {
          protectedCb
        }
      case None=>
        Forbidden("Currently only internal-tier keys are allowed access")
    }
  }

}
