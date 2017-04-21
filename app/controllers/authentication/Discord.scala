package controllers.authentication

import com.tw.discord.api.{DiscordApi, RequestFailedException}
import com.tw.discord.api.user.DiscordUser
import com.typesafe.config.{Config, ConfigFactory}
import controllers.AuthenticationCookie
import controllers.composition.Authenticated
import play.api.Logger
import play.api.db.Database
import play.api.libs.ws._
import play.api.mvc._
import services.intern.NotificationService
import services.intern.database.{User, UserService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Discord trait defines actions that handle discord OAuth login with provided [[DiscordApi]]
  */
trait Discord extends Controller {
  protected val config: Config = ConfigFactory.load();

  protected val Auth: Authenticated
  protected val ws: WSClient
  protected val discordApi: DiscordApi = new DiscordApi(ws, config.getString("discord.user.agent.url"),
    config.getString("discord.user.agent.version"),
    config.getString("discord.client.id"), config.getString("discord.client.secret"))

  protected val db: Database
  implicit val context: ExecutionContext

  private def serverHost: String = config.getString("server.host")

  private def redirectUrl: String = serverHost + "oauth/authorize/discord"

  /**
    * Login with discord oauth api action
    */
  def loginWithOauth() = Auth { implicit request =>
    request.user match {
      case None =>
        val authorizeParams = discordApi.OAuth2Helper.buildAuthorizeParam(redirectUrl)
        Logger.info(s"Discord OAuth login: ${discordApi.authorizeUrl} [${authorizeParams}]")
        Redirect(discordApi.authorizeUrl, authorizeParams)
      case Some(_) => Redirect(controllers.routes.HomeController.index())
    }
  }

  /**
    * Handle the oauth authorization response.<br/>
    * If response contains the code, send the /token request to get the token.
    * With the token, we access the user account and then try yo find this user in the data base.
    */
  def handleAuthorize() = Auth.async(BodyParsers.parse.default) { implicit request =>

    request.user match {
      case None =>
        request.getQueryString("code") match {
          // find code parameter
          case Some(code) => {
            // access token request
            Logger.info(s"OAuth code: $code")
            discordApi.callAccessToken(code, redirectUrl) flatMap {
              token =>
                val userFuture = discordApi.getDiscordUser(token)
                userFuture map {
                  user: DiscordUser => {
                    Logger.debug(s"Discord user: $user")
                    // We ask discord for email
                    db.withConnection(implicit conn => {
                      val dbUser = UserService.findByLoginOrMail(user.email.get)
                      dbUser match {
                        case Some(u) => Logger.debug(s"Discord User $user already exist in data base $u")
                          Redirect(controllers.routes.HomeController.index()).withNewSession
                            .withCookies(AuthenticationCookie.generateCookie(u))
                        case None => {
                          Logger.info(s"Create new user from discord credentials: $user")
                          // todo randomize password generation
                          val createdUser: User = UserService.createUser(user, "123")
                          NotificationService.notify("New user created")
                          Redirect(controllers.routes.HomeController.index()).withNewSession
                            .withCookies(AuthenticationCookie.generateCookie(createdUser))
                        }
                      }
                    })
                  }
                } recover { case t: RequestFailedException =>
                  Logger.error("Failed to call access token", t)
                  Redirect(controllers.routes.HomeController.index())
                }
            }
          }
          case None => {
            Logger.error("Failed to find 'code' param from discord response")
            Logger.debug(s"${request.uri} : ${request.body}")
            Future.apply(Redirect(controllers.routes.HomeController.index()))
          }
        }
    }
  }

  def handleToken() = Auth {
    implicit request =>
      println(request.uri)
      Redirect(controllers.routes.HomeController.index())
  }
}
