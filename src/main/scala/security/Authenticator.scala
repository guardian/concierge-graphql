package security

import cats.effect.IO
import org.http4s.{Request, Response}

trait Authenticator {
  def limitByTier(req:Request[IO], minTier:UserTier)(protectedCb: UserTier => IO[Response[IO]]):IO[Response[IO]]
}
