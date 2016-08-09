package controllers.composition

import play.api.mvc.{Request, WrappedRequest}
import services.database.User

class AuthenticatedRequest[A](val user: Option[User], val request: Request[A]) extends WrappedRequest[A](request)
