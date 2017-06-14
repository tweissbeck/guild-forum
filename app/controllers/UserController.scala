package controllers

import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import controllers.composition.{AdminAction, Authenticated}
import forms.{AdminEditForm, SignInForm}
import play.api.data.Forms._
import play.api.data.{Form, OptionalMapping}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.intern.database.{User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi,
                               Auth: Authenticated, Admin: AdminAction) extends Controller with I18nSupport {

  val signInForm = Form(
    mapping(
      "firstName" -> nonEmptyText(1, 60),
      "lastName" -> nonEmptyText,
      "login" -> OptionalMapping(text),
      "mail" -> email,
      "password" -> nonEmptyText
    )(SignInForm.apply)(SignInForm.unapply)
  )

  val editForm = Form(
    mapping(
      "id" -> longNumber,
      "lastName" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "login" -> OptionalMapping(text),
      "email" -> nonEmptyText,
      "password" -> OptionalMapping(text),
      "isAdmin" -> boolean
    )(AdminEditForm.apply)(AdminEditForm.unapply))

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

  def convertUserToEdit(user: User): AdminEditForm = {
    AdminEditForm(user.id, user.lastName, user.firstName, user.login, user.mail, None, user.admin)
  }

  /** ******************************************************************************************************************
    * Action
    * *****************************************************************************************************************/

  def list() = Admin { implicit request =>
    db.withConnection { implicit conn =>
      Ok(views.html.user.admin.list(request.admin, UserService.list()))
    }
  }

  def edit(id: String) = Admin { implicit request =>
    db.withConnection { implicit conn =>
      try {
        val user: Option[User] = UserService.findById(id.toLong)
        user match {
          case Some(_) => Ok(views.html.user.admin.edit(request.admin, editForm.fill(convertUserToEdit(user.get))))
          case None => NotFound
        }
      } catch {
        case e: NumberFormatException => ???
      }
    }
  }

  def persistModification() = TODO

  def delete(id: String) = TODO

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
  def signIn() = Action { implicit request =>
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

}
