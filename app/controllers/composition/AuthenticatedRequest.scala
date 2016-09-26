package controllers.composition

import play.api.mvc.{Request, WrappedRequest}
import services.database.User

/** *
  * A request that hold a user if a logged in user can be find on request (header/cookie)
  *
  * @param user
  * @param request
  * @tparam A
  */
class AuthenticatedRequest[A](val user: Option[User], val request: Request[A]) extends WrappedRequest[A](request)
