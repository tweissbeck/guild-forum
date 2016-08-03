package controllers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.database.{User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {

  implicit val userWrites = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.id,
      "login" -> user.login,
      "mail" -> user.mail,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "createAt" -> user.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
  }

  def list() = Action {
    val loggedIn: User = User(0, Some("fakeLogin"), "", "", "aka@gmail.com", LocalDateTime.now(), None, false, "akka", "challenge", "01")

    db.withConnection { implicit conn =>
      Ok(views.html.user.list(loggedIn, UserService.list()))
    }
  }

  def jsonList() = Action {
    db.withConnection { implicit conn =>
      val users = UserService.list()
      val json = Json.toJson(users)
      Ok(json)
    }
  }

  def login() = Action {
    Ok("a")
  }

}
