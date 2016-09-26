package controllers.authentication

import com.typesafe.config.ConfigFactory
import controllers.composition.Authenticated
import controllers.routes
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws._
import play.api.mvc._
import services.discord.token.BearerToken
import services.discord.{AccessToken, DiscordApi}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by Thierry on 20/08/2016.
  */
trait Discord extends Controller {

  val Auth: Authenticated
  val ws: WSClient
  val discordApi: DiscordApi
  implicit val context: ExecutionContext


  def loginWithOauth() = Auth { implicit request =>
    request.user match {
      case None => {
        val scopes = Seq("identify", "email", "guilds");
        val clientId = ConfigFactory.load().getString("authentication.oauth.client.id")
        val scopeParam: String = scopes.mkString(" ")
        val params: Map[String, Seq[String]] = Map("response_type" -> Seq("code"),
          "client_id" -> Seq(clientId),
          "redirect_uri" -> Seq("http://localhost:9000/oauth/authorize/discord"),
          "scope" -> Seq(scopeParam),
          "client_secret" -> Seq("Rz1L6auRIJPZgTECBc2-CMYniDnuxNvK")
        )

        Redirect(discordApi.getAuthorizeUrl(), params)
      }
      case Some(u) => Redirect(routes.HomeController.index())
    }
  }

  /**
    * Handle the oauth authorization response.<br/>
    * If response contains the code, send the /token request to get the token.
    * With the token, we access the user account and then try yo find this user in the data base.
    */
  def handleAuthorize() = Auth.async(BodyParsers.parse.default) { implicit request =>

    def getToken(code: String): Future[AccessToken] = {

      def buildAccessToken(json: JsValue): AccessToken = {
        AccessToken((json \ "access_token").get.as[String],
          (json \ "expires_in").get.as[Long],
          (json \ "refresh_token").get.as[String],
          (json \ "token_type").get.as[String]
        )
      }

      val request = discordApi.getAccessTokenRequest()
      val response = discordApi.getAccessToken(request, code)
      response.map {
        resp => {
          resp.status match {
            case 200 => val json = resp.json
              Logger.info(Json.prettyPrint(json))
              val accessToken: AccessToken = buildAccessToken(json)
              accessToken
            case _ =>
              throw new Exception(s"Access token request to ${request.url} failed with status ${resp.statusText}. Response: ${resp.json}")
          }
        }
      }
    }


    request.user match {
      case None =>
        request.getQueryString("code") match {
          // find code parameter
          case Some(code) => {
            // access token request
            Logger.info("code => " + code)

            getToken(code) map {
              token: AccessToken => {
                val userRequest = discordApi.getUserRueqest(new BearerToken(token.accessToken))
                val userResponse = userRequest.get()
                userResponse map {
                  response =>
                    Logger.info(userRequest.url)
                    Logger.info(Json.prettyPrint(response.json))
                    response.status match {
                      case 200 => Logger.info("200")
                      case _ => Logger.error(s"${response.status}")
                    }
                }
                Redirect(routes.HomeController.index())
              }
            } recover { case t: Throwable =>
              Logger.error("Get token failed", t)
              Redirect(routes.HomeController.index())
            }
          }
          case None => {
            Logger.error(s"Failed to get code from request: ${request.uri}")
            Future(Redirect(routes.HomeController.index()))
          }
        }
      case Some(_) => {
        Future(Redirect(routes.HomeController.index()))
      }
    }

  }

  def handleToken() = Auth {
    implicit request =>
      println(request.uri)
      Redirect(routes.HomeController.index())
  }
}
