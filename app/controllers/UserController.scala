package controllers

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import controllers.composition.Authenticated
import forms.SignInForm
import play.api.data.Forms._
import play.api.data.{Form, OptionalMapping}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.database.{User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi,
                               Auth: Authenticated) extends Controller with I18nSupport {

  val signInForm = Form(
    mapping(
      "firstName" -> nonEmptyText(1, 60),
      "lastName" -> nonEmptyText,
      "login" -> OptionalMapping(text),
      "mail" -> email,
      "password" -> nonEmptyText
    )(SignInForm.apply)(SignInForm.unapply)
  )

  /** JSON serialization of User writes */
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

  def list() = Auth { request =>
    request.user match {
      case Some(u) => db.withConnection { implicit conn =>
        Ok(views.html.user.list(u, UserService.list()))
      }
      case None => Redirect(routes.AuthenticationController.login())
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
          // create the user
          // TODO handle exception like unique constraints
          val createdUser = UserService.createUser(data)
          // Redirect the user to login page
          Redirect(routes.HomeController.index())
            .withNewSession
            .withCookies(AuthenticationCookie.generateCookie(createdUser))
        }
      }
    )
  }

  def view() = Auth {
    implicit request =>
      request.user match {
        case Some(u) =>
          Ok(views.html.user.view(u, signInForm))
        case None => Redirect(routes.AuthenticationController.login())
      }
  }

}
