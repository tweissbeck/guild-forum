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
          "scope" -> Seq(scopeParam),
          "client_secret" -> Seq("Rz1L6auRIJPZgTECBc2-CMYniDnuxNvK")
        )
        Redirect("https://discordapp.com/api/oauth2/authorize", params)
      }
      case Some(u) => Redirect(routes.HomeController.index())
    }
  }

  /**
   * Handle the oauth authorization response.<br/>
   * If response contains the code, send the /token request to get the token.
   * With the token, we access the user account and then try yo find this user in the data base.
   */
  def handleAuthorize() = Auth { implicit request =>
    request.user match {
      case None =>
        request.getQueryString("code") match {
          case Some(c) =>
            Logger.info("code => " + c)
            val params: Map[String, Seq[String]] = Map(
              "grant_type" -> Seq("authorization_code"),
              "code" -> Seq(c),
              "redirect_uri" -> Seq("http://localhost:9000/oauth/authorize/discord"),
              "client_id" -> Seq(ConfigFactory.load().getString("authentication.oauth.client.id")),
              "client_secret" -> Seq("Rz1L6auRIJPZgTECBc2-CMYniDnuxNvK")
            )

            val request = ws.url("https://discordapp.com/api/oauth2/token").withHeaders("Content-Type" -> "application/x-www-form-urlencoded").post(params)
            request.map {
              response =>
                response.status match {
                  case 200 => {

                  }
                  case _ => {

                  }
                }
            }
            Redirect(routes.HomeController.index())
          case None => Redirect(routes.AuthenticationController.login()).flashing("error" -> "oauth.error.code.missing")
        }
      case Some(u) => Redirect(routes.HomeController.index())
    }
  }

  def handleToken() = Auth { implicit request =>
    println(request.uri)
    Redirect(routes.HomeController.index())
  }
}
