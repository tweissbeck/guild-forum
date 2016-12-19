package controllers.forum

import javax.inject.Inject

import controllers.composition.Authenticated
import play.api.db.Database
import play.api.mvc.Controller
import services.database.Topic

/**
  * Controller that handle forum pages
  */
class ForumController @Inject()(db: Database, Auth: Authenticated) extends Controller {

  def index() = Auth {
    implicit request => {
      db.withConnection { connection =>
        request.user match {
          case Some(u) => Topic.getRoot(u)(connection)
          case None => Topic.getPublic()(connection)
        }
      }
      Ok
    }
  }
}
