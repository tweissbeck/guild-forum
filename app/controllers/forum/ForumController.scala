package controllers.forum

import javax.inject.Inject

import controllers.composition.Authenticated
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.mvc.Controller
import services.intern.database.Topic

/**
  * Controller that handle forum pages
  */
class ForumController @Inject()(db: Database, Auth: Authenticated, implicit val messagesApi: MessagesApi)
  extends Controller {

  def index() = Auth {
    implicit request => {
      db.withConnection { connection =>
        val categories = request.user match {
          case Some(u) => Topic.getRoot(u)(connection)
          case None => Topic.getPublic()(connection)
        }
        Ok(views.html.forum.index(request.user, categories))
      }
    }
  }
}
