package controllers.authentication

import com.typesafe.config.ConfigFactory
import controllers.composition.Authenticated
import controllers.routes
import play.api.Logger
import play.api.libs.ws._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

/**
 * Created by Thierry on 20/08/2016.
 */
trait Discord extends Controller {

  val Auth: Authenticated
  val ws: WSClient
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
          "scope" -> Seq(scopeParam)
        )
        Redirect("https://discordapp.com/api/oauth2/authorize", params)
      }
      case Some(u) => Redirect(routes.HomeController.index())
    }
  }

  /**
   * Handle the oauth authorization response. Send the access token request then try to get the associated user in our
   * database in order to authenticate him.
   */
  def handleAuthorize() = Auth { implicit request =>
    request.user match {
      case None =>
        request.getQueryString("code") match {
          case Some(c) =>
            val params: Map[String, Seq[String]] = Map(
              "grant_type" -> Seq("authorization_code"),
              "code" -> Seq(c)//,
              //"redirect_uri" -> Seq("http://localhost:9000/oauth/token/discord") //,
              //"client_id" -> Seq(ConfigFactory.load().getString("authentication.oauth.client.id"))
            )

            val request = ws.url("https://discordapp.com/api/oauth2/token").withHeaders("Content-Type" -> "application/x-www-form-urlencoded").post(params)
            request.map {
              response =>
                Logger.info("Status: "+response.status.toString)
                Logger.info("Response body: "+response.body)
            }
            Redirect(routes.HomeController.index())
          case None => Redirect(routes.AuthenticationController.login()).flashing("error" -> "oauth.error.code.missing")
        }
      case Some(u) => Redirect(routes.HomeController.index())
    }
  }

  def handleToken() = Auth { implicit request =>
    ???
  }
}
