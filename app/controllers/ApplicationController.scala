package controllers

import javax.inject.Inject

import controllers.composition.{AdminAction, Authenticated}
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.mvc.Controller
import services.intern.ApplicationService

/**
  * @author tweissbeck
  */
class ApplicationController @Inject()(db: Database, Auth: Authenticated, Admin: AdminAction,
                                      implicit val messageApi: MessagesApi) extends Controller {

  /**
    * List current application
    *
    * @return
    */
  def list() = Admin { implicit request =>
    db.withConnection {
      implicit connection =>
        ApplicationService.getNew()
    }

    ???
  }

  def detail(id: String) = Admin {
    implicit request => ???
  }

}
