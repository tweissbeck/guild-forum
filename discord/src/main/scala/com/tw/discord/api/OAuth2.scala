package com.tw.discord.api

import com.tw.discord.api.authorize.Token
import com.tw.discord.api.user.OAuth2DiscordScopes
import play.api.libs.json.JsValue
import play.api.libs.ws.WSRequest

/**
  * OAuth 2 helper for discord authentication
  */
class OAuth2(val clientId: String, val secret: String, val userAgent: String) {

  /**
    * Build authorize parameters
    *
    * @param redirectUrl where oauth provider will redirect client browser
    * @param scopes      (Optional)
    */
  def buildAuthorizeParam(redirectUrl: String, scopes: Seq[OAuth2DiscordScopes.Value] = Seq(
    OAuth2DiscordScopes.IDENTITY, OAuth2DiscordScopes.EMAIL, OAuth2DiscordScopes.GUILDS)): Map[String, Seq[String]] = {
    //val scope: String = scopes.map(it => it.toString).reduce((l, r) => s"$l+$r")
    Map(
      "client_id" -> Seq(clientId),
      "redirect_uri" -> Seq(redirectUrl),
      "scope" -> Seq(scopes.map(it => it.toString).mkString(" ")),
      "client_secret" -> Seq(secret),
      "response_type" -> Seq("code")
    )
  }

  def parseAccessToken(json: JsValue): AccessToken = {
    AccessToken((json \ "access_token").get.as[String],
      (json \ "expires_in").get.as[Long],
      (json \ "refresh_token").get.as[String],
      (json \ "token_type").get.as[String]
    )
  }

  def includeHeaders(request: WSRequest, token: Token): WSRequest = {
    request.withHeaders("Authorization" -> token.toString, "User-Agent" -> userAgent)
  }


}
