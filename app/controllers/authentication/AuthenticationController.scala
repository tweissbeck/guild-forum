package controllers.authentication

import javax.inject.Inject

import api.authentication.LoginResponse
import api.{ErrorResponse, Response}
import controllers.composition.Authenticated
import controllers.{AuthenticationCookie, FlashConstant, JWT}
import forms.LoginForm
import play.api.data.Forms._
import play.api.data._
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsResultException, JsValue, Json, Writes}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller, DiscardingCookie, Result}
import play.api.{Environment, Logger}
import services.intern.AuthenticationService

import scala.concurrent.ExecutionContext

/**
  * Authentication controller
  */
class AuthenticationController @Inject()(val db: Database, implicit val messagesApi: MessagesApi,
                                         val Auth: Authenticated, val environment: Environment,
                                         val ws: WSClient, implicit val context: ExecutionContext)
  extends Controller with I18nSupport with Discord {

  // Let api retrieve discord credential from config
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
        // FIXME we don't check the token validity here !?
        Logger.info("User already logged")
        Redirect(controllers.routes.HomeController.index())
      }
      case None => {
        val uri = request.flash.get(FlashConstant.REQUESTED_RESOURCE)
        val result = Ok(views.html.user.login(loginForm))
        uri match {
          case Some(_) => result.flashing(FlashConstant.REQUESTED_RESOURCE -> uri.get)
          case None => result
        }
      }
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
              def applyCookie(result: Result): Result = {
                result.withNewSession.withCookies(AuthenticationCookie.generateCookie(user))
              }

              if (request.flash.get(FlashConstant.REQUESTED_RESOURCE).nonEmpty) {
                applyCookie(Redirect(request.flash.get(FlashConstant.REQUESTED_RESOURCE).get))
              } else {
                applyCookie(Redirect(controllers.routes.HomeController.index()))
              }
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
      case Some(u) => Redirect(controllers.routes.HomeController.index())
        .discardingCookies(DiscardingCookie(AuthenticationCookie.NAME))
        .withNewSession
      case None => Redirect(controllers.routes.HomeController.index())
    }
  }

  implicit val responseWrite = new Writes[Response] {
    override def writes(response: Response): JsValue = {
      // Is there a better way to do this ? Using instance of is not cool.
      response match {
        case login: LoginResponse =>
          loginResponseWrites.writes(login)
        case error: ErrorResponse =>
          errorResponseWrites.writes(error)
        case _ =>
          Json.obj(
            "code" -> response.code
          )
      }
    }
  }

  implicit val loginResponseWrites: Writes[LoginResponse] = new Writes[LoginResponse] {
    def writes(response: LoginResponse) = Json.obj(
      "code" -> response.code,
      "token" -> response.token
    )
  }

  implicit val errorResponseWrites: Writes[ErrorResponse] = new Writes[ErrorResponse] {
    override def writes(response: ErrorResponse): JsValue = Json.obj(
      "code" -> response.code,
      "error" -> response.error,
      "detail" -> response.detail
    )
  }

  /**
    * Handle json login request
    */
  def loginApi() = Action {
    implicit request =>

      /**
        * Try to log the user with data base
        */
      def doLogin[T >: Response](body: Option[JsValue]): T = {
        body match {
          case Some(auth) =>
            val login = (auth \ "login").as[String]
            val password = (auth \ "password").as[String]
            db.withConnection {
              implicit conn =>
                val user = AuthenticationService.authenticateUser(login, password)
                user match {
                  case Some(user) =>
                    val token = JWT.build(user)
                    LoginResponse(200, token)
                  case None => ErrorResponse(401, "authentication_failed")
                }
            }
          case None =>
            new ErrorResponse(400, "invalid_request", "Failed to parse request as json")
        }
      }

      try {
        val body = request.body.asJson
        Ok(Json.toJson(doLogin(body)))
      }
      catch {
        // If json object in request does contains required field.
        case e: JsResultException =>
          Logger.error(e.getMessage, e)
          Ok(Json.toJson(new ErrorResponse(400, "invalid_request", e.getMessage)))
      }
  }

}
