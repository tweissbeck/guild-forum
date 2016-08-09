package controllers.composition

import javax.inject.Inject

import controllers.AuthenticationCookie
import play.api.Logger
import play.api.db.Database
import play.api.mvc.{ActionBuilder, Request, Result}
import services.database.UserService

import scala.concurrent.Future

/**
 * Authenticated action builder: request an Option[User]
 */
class Authenticated @Inject()(implicit db: Database) extends ActionBuilder[AuthenticatedRequest] {

  val AUTHENTICATION_TOKEN_KEY = AuthenticationCookie.NAME

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {

    val authenticationCookie = request.cookies.get(AUTHENTICATION_TOKEN_KEY)
    authenticationCookie match {
      case Some(c) => {
        try {
          val userId = c.value.toLong
          db.withConnection { implicit conn =>
            val user = UserService.findById(userId)
            block(new AuthenticatedRequest[A](user, request))
          }
        }
        catch {
          case e: Exception => {
            Logger.error(s"Fail to parse cookie: ${c.value}", e)
            block(new AuthenticatedRequest[A](None, request))
          }
        }

      }
      case None => {
        val token = request.headers.get(AUTHENTICATION_TOKEN_KEY)
        token match {
          case Some(t) =>
            // TODO extract user from header
            block(new AuthenticatedRequest(None, request))
          case None => block(new AuthenticatedRequest(None, request))
        }
      }
    }
  }
}
