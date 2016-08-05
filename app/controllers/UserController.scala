package controllers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import forms.SignInForm
import play.api.data.Forms._
import play.api.data.{Form, OptionalMapping}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.database.{User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val signInForm = Form(
    mapping(
      "firstName" -> nonEmptyText(1, 60),
      "lastName" -> nonEmptyText,
      "login" -> OptionalMapping(text),
      "mail" -> email,
      "password" -> nonEmptyText
    )(SignInForm.apply)(SignInForm.unapply)
  )


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
    val loggedIn: User = User(0, Some("fakeLogin"), "", "", "aka@gmail.com", LocalDateTime.now(), None, false, "akka", "01")

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

  /**
   * GET SignIn
   */
  def signIn() = Action {
    Ok(views.html.user.signIn(signInForm))
  }

  /**
   * POST SignIn
   */
  def signInPost() = Action { implicit request =>
    signInForm.bindFromRequest().fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.user.signIn(formWithErrors))
      },
      data => {
        db.withConnection { implicit conn =>
          val createdUser = UserService.createUser(data)
          Redirect(routes.HomeController.index())
        }
      }
    )
  }

}
