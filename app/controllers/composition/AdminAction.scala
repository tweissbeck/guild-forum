package controllers.composition


import com.google.inject.Inject
import play.api.Logger
import play.api.db.Database
import play.api.mvc._
import services.intern.database.{AdminUser, User, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

/**
  * Action builder for action accessible by admin.
  */
class AdminAction @Inject()(implicit db: Database, cc: ControllerComponents)
  extends ActionBuilder[AdminRequest, AnyContent] {

  override protected def executionContext: ExecutionContext = cc.executionContext

  override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: (AdminRequest[A]) => Future[Result]): Future[Result] = {

    def handleForbidden(user: Option[User] = None): Future[Result] = {
      Future {
        user match {
          case Some(u) => Logger
            .info(s"${request.uri} redirected to login page cause user: ${u.login} is not admin: ${u.admin}")
          case None => Logger.info(s"${request.uri} redirected to login page cause no user")
        }

        Results.Redirect(controllers.authentication.routes.AuthenticationController.login())
          .flashing("actionRequested" -> s"${request.uri}")
      }
    }

    val userId = getUserFromRequest(request)
    userId match {
      case Some(id) =>
        db.withConnection { implicit conn =>
          val user = UserService.findById(id)
          user match {
            case Some(u) =>
              u match {
                case admin: AdminUser => block(new AdminRequest[A](admin, request));
                case _ => handleForbidden(user)
              }
            case None => handleForbidden()
          }
        }
      case None =>
        handleForbidden()
    }
  }
}
