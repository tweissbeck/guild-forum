package controllers

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.database.{User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def list() = Action {
    val loggedIn: User = User(0, "", "aka@gmail.com", LocalDateTime.now(), None)

    db.withConnection { implicit conn =>
      Ok(views.html.user.list(loggedIn, UserService.list()))
    }
  }

}
