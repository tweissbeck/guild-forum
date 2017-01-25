package com.tw.discord.api

import java.nio.file.Paths

import com.typesafe.config.{Config, ConfigFactory}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}

import scala.concurrent.Future

/**
  * Discord api entry point
  */
abstract class DiscordApi(val _agentUrl: Option[String], val _version: Option[String], val _clientId: Option[String],
                          val _secret: Option[String]) {

  val ws: WSClient
  private val config: Config = ConfigFactory.load()
  private val userAgentUrl: String = if (_agentUrl.isDefined) _agentUrl.get else config
    .getString("discord.user.agent.url")
  private val userAgentVersion: String = if (_version.isDefined) _version.get else config
    .getString("discord.user.agent.version")
  private val userAgent: String = s"DiscordBot ($userAgentUrl, $userAgentVersion)"
  private val discordHost = "https://discordapp.com/api"

  private val userPath = "users"
  private val accessTokenPath = "oauth2/token"
  private val authorizePath = "oauth2/authorize"
  private val clientId: String = if (_clientId.isDefined) _clientId.get else config.getString("discord.client.id")
  private val secret: String = if (_secret.isDefined) _secret.get else config.getString("discord.client.secret")

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
    val tokenUrl = Paths.get(discordHost, accessTokenPath).toUri.toURL.toExternalForm
    println(tokenUrl)
    val request = ws.url(tokenUrl).withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    f(request).post(params)

  }

  /**
    * Parse the access token response
    *
    * @param json the response as [[JsValue]]
    * @return an instance of [[AccessToken]]
    */
  def parseAccessToken(json: JsValue): AccessToken = {
    AccessToken((json \ "access_token").get.as[String],
      (json \ "expires_in").get.as[Long],
      (json \ "refresh_token").get.as[String],
      (json \ "token_type").get.as[String]
    )
  }

  def getUser(token: AccessToken): Future[WSResponse] = {
    val url = s"$discordHost/$userPath/@me"
    val request = ws.url(url).withHeaders("Authorization" -> token.toString, "User-Agent" -> this.userAgent)
    request.get()
  }
}
