package controllers.composition

import javax.inject.Inject

import controllers.{AuthenticationCookie, JWT}
import play.api.Logger
import play.api.db.Database
import play.api.mvc._
import services.intern.database.UserService

import scala.concurrent.Future

/**
  * Authenticated action builder: request an Option[User]
  */
class Authenticated @Inject()(implicit db: Database) extends ActionBuilder[AuthenticatedRequest] {

  val AUTHENTICATION_TOKEN_KEY = AuthenticationCookie.NAME

  def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]) = {

    /**
      * Validate the token and find the user. If token is not valid, or user not exist, block the request
      *
      * @param token JWT Token as String
      * @return
      */
    def handleToken(token: String): Future[Result] = {
      val userId: Option[Long] = JWT.validateJWT(token)
      userId match {
        case Some(id) =>
          //val userId = c.value.toLong
          db.withConnection { implicit conn =>
            val user = UserService.findById(id)
            block(new AuthenticatedRequest[A](user, request))
          }
        case None =>
          Logger.info("JWT is not valid")
          block(new AuthenticatedRequest[A](None, request))
      }
    }

    val authenticationCookie: Option[Cookie] = request.cookies.get(AUTHENTICATION_TOKEN_KEY)
    authenticationCookie match {
      case Some(c) => {
        try {
          handleToken(c.value)
        }
        catch {
          case e: Exception => {
            Logger.error(s"Fail handle request", e)
            block(new AuthenticatedRequest[A](None, request))
          }
        }

      }
      case None => {
        val token: Option[String] = request.headers.get(AUTHENTICATION_TOKEN_KEY)
        token match {
          case Some(t) =>
            handleToken(t)
          case None => block(new AuthenticatedRequest(None, request))
        }
      }
    }
  }
}
