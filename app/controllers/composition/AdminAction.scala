package controllers.composition

import javax.inject.Inject

import play.api.db.Database
import play.api.mvc.{ActionBuilder, Request, Result}
import play.mvc.Controller
import services.database.{AdminUser, UserService}

import scala.concurrent.Future

/**
  * Action builder for action accessible by admin.
  */
class AdminAction @Inject()(implicit db: Database) extends ActionBuilder[AdminRequest] {
  override def invokeBlock[A](request: Request[A], block: (AdminRequest[A]) => Future[Result]): Future[Result] = {

    def handleForbidden(): Future[Result] = {
      play.api.mvc.Results.Forbidden()
      ???
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
                case _ => handleForbidden()
              }
            case None => handleForbidden()
          }
        }
      case None =>
        handleForbidden()
    }
  }
}
