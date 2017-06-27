package controllers

import java.sql.SQLException
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}

import api.{ErrorResponse, Response}
import controllers.composition.{AdminAction, Authenticated}
import controllers.front.UserList
import forms.{AdminEditForm, SignInForm}
import play.api.Logger
import play.api.data.Forms._
import play.api.data.{Form, OptionalMapping}
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.IdEncryptionUtil
import services.intern.database.{AdminUser, CommonUser, User, UserService}


@Singleton
class UserController @Inject()(db: Database, implicit val messagesApi: MessagesApi,
                               Auth: Authenticated, Admin: AdminAction) extends Controller with I18nSupport {

  implicit private def usersToUsersList(users: Seq[User]): Seq[(UserList, Boolean)] = {
    users.map { u => (new UserList(u), !u.id.equals(0)) }
  }

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
        val user: Option[User] = UserService.findById(IdEncryptionUtil.decodeLong(id))
        user match {
          case Some(_) => Ok(views.html.user.admin.edit(request.admin, editForm.fill(convertUserToEdit(user.get)), id))
          case None => NotFound(views.html.commons.ResourceNotFound(Some(request.admin), id, None))
        }
      } catch {
        case _: NumberFormatException => NotFound(views.html.commons.ResourceNotFound(Some(request.admin), id, None))
      }
    }
  }

  def persistModification(id: String) = Admin {
    implicit request =>
      db.withConnection {
        implicit connection =>
          val user = UserService.findById(IdEncryptionUtil.decodeLong(id))
          user match {
            case Some(u) => {
              val form = editForm.bindFromRequest()
              form.fold(
                errors => {
                  Logger.debug("Validation failed for update user form.")
                  Ok(views.html.user.admin.edit(request.admin, form, id))
                },
                data => {
                  val other = if (form.get.isAdmin) AdminUser(u.id, data.login, data.firstName, data.lastName,
                    data.email, u.createdAt, u.lastLogin, data.password.getOrElse(u.password),
                    u.salt) else CommonUser(u.id, data.login,
                    data.firstName, data.lastName, data.email, u.createdAt, u.lastLogin,
                    data.password.getOrElse(u.password), u.salt)
                  val diffs: Map[String, String] = u.diff(other)
                  if (diffs.isEmpty) {
                    Logger.debug(s"User ${u.mail}:${u.id} has not been modified because no update was detected")
                    Redirect(controllers.routes.UserController.list())
                  } else {
                    if (UserService.update(u.id, diffs) > 0) {
                      Logger.info(s"User with id updated on fields: ${diffs.mkString(", ")}")
                    }
                    Redirect(controllers.routes.UserController.list())
                      .flashing("SUCCESS" -> messagesApi.apply("user.update.success"))
                  }
                }
              )
            }
            case None => NotFound(views.html.commons.ResourceNotFound(Some(request.admin), id, None))
          }
      }
  }

  def delete(id: String) = Admin { implicit request =>
    val idAsLong = IdEncryptionUtil.decodeLong(id)
    db.withConnection {
      implicit conn => {
        val user = UserService.findById(idAsLong)

        val response = if (user.isDefined) {
          val userToDelete = user.get
          // logged in admin can't delete himself
          if (!userToDelete.id.equals(request.admin.id)) {
            Logger.info(s"User ${request.admin.login}:${request.admin.id} have delete the user ${
              userToDelete.login
            }:${userToDelete.id}")
            try {
              UserService.delete(idAsLong)
              Response.OK
            } catch {
              case e: SQLException =>
                Logger.error(s"Failed to delete user ${userToDelete.login}:${userToDelete.id}", e)
                ErrorResponse(503, ErrorResponse.DELETE_USER_FAILED, Some(e.getMessage))
            }
          } else {
            ErrorResponse(403, ErrorResponse.USER_SELF_DELETE)
          }
        } else {
          ErrorResponse(404, ErrorResponse.USER_NOT_EXIST)
        }
        response match {
          case error: ErrorResponse => error.error match {
            case ErrorResponse.DELETE_USER_FAILED =>
              InternalServerError(
                views.html.user.admin.delete(request.admin, ERROR, messagesApi.apply("user.admin.delete.error")))
            case ErrorResponse.USER_SELF_DELETE =>
              Forbidden(views.html.user.admin
                .delete(request.admin, WARN, messagesApi.apply("user.admin.delete.cant.delete.self")))
            case ErrorResponse.USER_NOT_EXIST =>
              NotFound(views.html.user.admin
                .delete(request.admin, ERROR, messagesApi.apply("user.admin.delete.user.notexist")))
          }
          case _: Response => Ok(views.html.user.admin.delete(request.admin, INFO,
            messagesApi.apply("user.admin.delete.ok", user.get.login.getOrElse(user.get.mail))))
        }
      }
    }
  }

  def jsonList() = Action {
    db.withConnection {
      implicit conn =>
        val users = UserService.list()
        val json = Json.toJson(users)
        Ok(json)
    }
  }

  /**
    * GET SignIn
    */
  def signIn() = Action {
    implicit request =>
      Ok(views.html.user.signIn(signInForm))
  }

  /**
    * POST SignIn
    */
  def signInPost() = Action {
    implicit request =>
      signInForm.bindFromRequest().fold(
        formWithErrors => {
          // binding failure, you retrieve the form containing errors:
          BadRequest(views.html.user.signIn(formWithErrors))
        },
        data => {
          db.withConnection {
            implicit conn =>
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
