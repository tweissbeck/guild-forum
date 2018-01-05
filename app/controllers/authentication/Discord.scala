package controllers.authentication

import com.tw.discord.api.user.DiscordUser
import com.tw.discord.api.{DiscordApi, RequestFailedException}
import com.typesafe.config.{Config, ConfigFactory}
import controllers.composition.Authenticated
import controllers.{AuthenticationCookie, FLASH_SCOPE}
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.libs.ws._
import play.api.mvc._
import play.api.{Environment, Logger, Mode}
import services.intern.NotificationService
import services.intern.database.{User, UserService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * Discord trait defines actions that handle discord OAuth login with provided [[DiscordApi]]
  */
trait Discord extends Controller {
  protected val config: Config = ConfigFactory.load()

  protected val Auth: Authenticated
  protected val ws: WSClient
  protected val messagesApi: MessagesApi
  /**
    * Play environment. Instance of this class have to given by implementations
    */
  protected val environment: Environment
  protected val discordApi: DiscordApi = new DiscordApi(ws, config.getString("discord.user.agent.url"),
    config.getString("discord.user.agent.version"),
    config.getString("discord.client.id"), config.getString("discord.client.secret"))

  protected val db: Database
  implicit val context: ExecutionContext

  private def serverHost: String = config.getString("server.host")

  private def redirectUrl: String = serverHost + "oauth/authorize/discord"

  /**
    * Define the size of the generated password
    */
  private val PASSWORD_SIZE = 8

  /**
    * Login with discord oauth api action
    */
  def loginWithOauth() = Auth { implicit request =>
    request.user match {
      case None =>
        val authorizeParams = discordApi.OAuth2Helper.buildAuthorizeParam(redirectUrl)
        Logger.info(s"Discord OAuth login: ${discordApi.authorizeUrl} [$authorizeParams]")
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
          case Some(code) =>
            // access token request
            Logger.info(s"OAuth code: $code")
            discordApi.callAccessToken(code, redirectUrl) flatMap {
              token =>
                val userFuture = discordApi.getDiscordUser(token)
                userFuture map {
                  user: DiscordUser => {
                    Logger.debug(s"Discord user: $user")
                    // Check if the provided user is valid: verified & email not empty
                    if (user.verified.getOrElse(false) && user.email.isDefined) {
                      db.withConnection(implicit conn => {
                        val dbUser = UserService.findByLoginOrMail(user.email.get)
                        dbUser match {
                          case Some(u) => Logger.debug(s"Discord User $user already exist in data base $u")
                            Redirect(controllers.routes.HomeController.index()).withNewSession
                              .withCookies(AuthenticationCookie.generateCookie(u))
                          case None =>
                            Logger.info(s"Create new user from discord credentials: $user")
                            val generatedPassword = Random.alphanumeric.take(PASSWORD_SIZE).mkString
                            // log the password in dev environment to be able to log with the new created user easily
                            if (Mode.Dev == environment.mode) {
                              Logger.debug(s"Generated password for user ${user.username}: $generatedPassword")
                            }
                            val createdUser: User = UserService.createUser(user, generatedPassword)
                            NotificationService.notify("New user created")
                            Redirect(controllers.routes.HomeController.index()).withNewSession
                              .withCookies(AuthenticationCookie.generateCookie(createdUser))
                        }
                      })
                    }
                    // User not complete, do not trying to match this user with our data base, refused authentication
                    else {
                      val messageKey = if (!user.verified.getOrElse(
                        false)) "login.form.discord.user.not.verified" else "login.form.discord.user.no.email"
                      Redirect(controllers.authentication.routes.AuthenticationController.login()).withNewSession
                        .flashing(FLASH_SCOPE.DISCORD_LOGIN_ERROR -> messagesApi.apply(messageKey))
                    }
                  }
                } recover { case t: RequestFailedException =>
                  Logger.error("Failed to call access token", t)
                  Redirect(controllers.routes.HomeController.index())
                }
            }
          case None =>
            Logger.error("Failed to find 'code' param from discord response")
            Logger.debug(s"${request.uri} : ${request.body}")
            Future.apply(Redirect(controllers.routes.HomeController.index()))

        }
    }
  }

  def handleToken() = Auth {
    implicit request =>
      println(request.uri)
      Redirect(controllers.routes.HomeController.index())
  }
}
