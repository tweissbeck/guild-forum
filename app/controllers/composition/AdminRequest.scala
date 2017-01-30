package controllers.composition

import play.api.mvc.{Request, WrappedRequest}
import services.intern.database.AdminUser

/**
  * Created by tweissbeck on 30/09/2016.
  */
class AdminRequest[A](val admin: AdminUser, val request: Request[A]) extends WrappedRequest[A](request)  {

}
