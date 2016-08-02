package controllers

import javax.inject.Inject

import forms.{LoginForm, SignInForm}
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Cookie, DiscardingCookie}
import services.AuthenticationService

/**
 * Authentication controller
 */
class AuthenticationController @Inject()(db: Database,
                                         implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {

  /** Login form */
  val loginForm = Form(
    mapping(
      "login" -> nonEmptyText,
      "pwd" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  val signInForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "login" -> OptionalMapping(text),
      "mail" -> email,
      "pwd" -> nonEmptyText
    )(SignInForm.apply)(SignInForm.unapply)
  )

  /** GET login, display the form */
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
   * POST : handle form submit, try to authenticate user.
   * Redirect to home page when user is authenticated
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
                  AuthenticationCookie.cookie(user.lastName)
                ).addingToSession("sessionId" -> user.lastName)
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
   * Helper to build a cookie that contains authentication data
   */
  object AuthenticationCookie {
    val NAME = "sessionId"

    def cookie(value: String): Cookie = Cookie(NAME, value, Some(16000), secure = false, httpOnly = true)
  }


}
