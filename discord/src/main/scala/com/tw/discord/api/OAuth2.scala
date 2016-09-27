package com.tw.discord.api

import javax.inject.Inject

import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

/**
  * OAuth 2 helper for discord authentication
  */
class OAuth2 @Inject()(ws: WSClient)(val clientId: String, val secret: String) {

  val authorizationUrl = "https://discordapp.com/api/oauth2/authorize"
  val tokenUrl = "https://discordapp.com/api/oauth2/token"

  def buildAuthorizeParam(redirectUrl: String, scopes: Seq[OAuth2DiscordScopes.Value] = Seq(
    OAuth2DiscordScopes.IDENTITY, OAuth2DiscordScopes.EMAIL, OAuth2DiscordScopes.GUILDS)): Map[String, Seq[String]] = {

    val _scopes = scopes.map(it => it.toString)
    Map(
      "client_id" -> Seq(clientId),
      "redirect_uri" -> Seq(redirectUrl),
      "scope" -> _scopes,
      "client_secret" -> Seq(secret)
    )
  }

  /**
    * Build access token request and send it to discord.
    *
    * @param code        [[String]]:     the code given buy OAuth2 provider
    * @param redirectUrl [[String]]: have to be the same provided in [[OAuth2.buildAuthorizeParam()]]
    * @param f           function ([[WSRequest]] => [[WSRequest]]) default to identity. Can be use to customize request
    * @return the response the [[WSResponse]] as a [[scala.concurrent.Future]]
    */
  def accessToken(code: String, redirectUrl: String, f: (WSRequest => WSRequest) = it => it): Future[WSResponse] = {
    val params: Map[String, Seq[String]] = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(code),
      "redirect_uri" -> Seq(redirectUrl),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(secret)
    )
    val request = ws.url(tokenUrl).withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    f(request).post(params)

  }

  def parseAccessToken(json: JsValue): AccessToken = {
    AccessToken((json \ "access_token").get.as[String],
      (json \ "expires_in").get.as[Long],
      (json \ "refresh_token").get.as[String],
      (json \ "token_type").get.as[String]
    )
  }


}
