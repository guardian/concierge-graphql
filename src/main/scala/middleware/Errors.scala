package middleware

import sangria.execution.UserFacingError

object Errors {
  case class PermissionDenied(msg: String) extends Exception with UserFacingError {
    override def getMessage: String = msg
  }
}
