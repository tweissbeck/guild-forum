package services.discord

import javax.inject.Inject

import com.google.inject.Singleton
import com.typesafe.config.ConfigFactory
import play.api.libs.ws._
import services.discord.token.Token

import scala.concurrent.Future


/**
  * If the access token request is valid and authorized, the
  * authorization server issues an access token and optional refresh
  * token
  *
  * @param accessToken  The access token issued by the authorization server.
  * @param expireIn     The lifetime in seconds of the access token.  For
  *                     example, the value "3600" denotes that the access token will
  *                     expire in one hour from the time the response was generated.
  *                     If omitted, the authorization server SHOULD provide the
  *                     expiration time via other means or document the default value.
  * @param refreshToken The refresh token, which can be used to obtain new
  *                     access tokens using the same authorization grant
  * @param tokenType    The type of the token issued
  */
case class AccessToken(accessToken: String, expireIn: Long, refreshToken: String, tokenType: String)

/**
  * Discord API helper
  */
trait DiscordApi {


  protected val ws: WSClient
  val userAgentUrl: String = "tw-scala-discord"
  val userAgentVersion: String = "1.0.0-SNAPSHOT"
  val userAgent: String = s"DiscordBot ($userAgentUrl, $userAgentVersion)"
  val discordHost = "https://discordapp.com/api"
  val userPath = "users"
  val accessTokenPath = "oauth2/token"
  val authorizePath = "oauth2/authorize"

  def getAuthorizeUrl(): String = {
    s"$discordHost/$authorizePath"
  }


  def getUserRequest(token: Token): WSRequest = {
    val url = s"$discordHost/$userPath/@me"
    ws.url(url).withHeaders("Authorization" -> token.toString, "User-Agent" -> this.userAgent)
  }

  /**
    * Build access token request
    *
    * @return the acces token request as WSRequest
    */
  def getAccessTokenRequest(): WSRequest = {
    ws.url(s"$discordHost/$accessTokenPath").withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
  }

  /**
    * Post access token request and return the response
    *
    * @param request the request
    * @param code    the code put in request post parameters
    * @return the WSResponse
    */
  def getAccessToken(request: WSRequest, code: String): Future[WSResponse] = {
    val params: Map[String, Seq[String]] = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(code),
      // TODO move this in config
      "redirect_uri" -> Seq("http://localhost:9000/oauth/authorize/discord"),
      "client_id" -> Seq(ConfigFactory.load().getString("authentication.oauth.client.id")),
      "client_secret" -> Seq(ConfigFactory.load().getString("authentication.oauth.secret"))
    )
    request.post(params)
  }

}

@Singleton
class DiscordService @Inject()(val ws: WSClient) extends DiscordApi {

}
