package security

import cats.effect.IO
import org.http4s.Request
import org.typelevel.ci.CIString

object KongHeader {
  val name = CIString("X-Consumer-Username")

  /**
   * Extracts the user tier, as passed by Kong, from the given request
   * @param req request
   * @return an Option containing the UserTier, or None if there is none available
   */
  def extractUserTier(req:Request[IO]):Option[UserTier] = {
    req.headers
      .get(name)
      .flatMap(h=>UserTier(h.head.value.substring(h.head.value.lastIndexOf(":") + 1)))
  }
}
