package controllers

import javax.inject.Inject

import controllers.composition.Authenticated
import forms.LoginForm
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, DiscardingCookie}
import services.AuthenticationService

/**
 * Authentication controller
 */
class AuthenticationController @Inject()(db: Database,
                                         implicit val messagesApi: MessagesApi,
                                         Auth: Authenticated) extends Controller with I18nSupport {

  /** Login form */
  val loginForm = Form(
    mapping(
      "login" -> nonEmptyText,
      "pwd" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )


  /** GET login form, display the form */
  def login() = Action { implicit request =>
    val cookie = request.cookies.get(AuthenticationCookie.NAME)
    cookie match {
      case Some(cookie) => {
        Logger.info("User already logged")
        Redirect(routes.HomeController.index())
      }
      case None => Ok(views.html.user.login(loginForm))
    }
  }

  /**
   * POST : handle login form submit, try to authenticate user.
   * Redirect to home page if user is authenticated
   */
  def loginPost() = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.user.login(formWithErrors))
      },
      userData => {
        db.withConnection { implicit connection =>
          val user = AuthenticationService.authenticateUser(userData.login, userData.pwd)
          user match {
            case Some(user) =>
              // TODO the cookie need be signed (JWT, ...) in progress
              Redirect(routes.HomeController.index())
                .withNewSession
                .withCookies(
                  AuthenticationCookie.generateCookie(user)
                )
            case None => {
              Ok(views.html.user.login(loginForm.withGlobalError("NotAuthenticated")))
                .discardingCookies(DiscardingCookie("sessionId"))
            }
          }
        }
      }
    )
  }

  /**
   * Disconnect the current user if any.
   */
  def logout() = Auth { implicit request =>
    request.user match {
      case Some(u) => Redirect(routes.HomeController.index())
        .discardingCookies(DiscardingCookie(AuthenticationCookie.NAME))
        .withNewSession
      case None => Redirect(routes.HomeController.index())
    }
  }

}
