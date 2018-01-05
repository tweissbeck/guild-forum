package com.tw.discord.api

import com.tw.discord.api.authorize.{BearerToken, BotToken, Token}
import com.tw.discord.api.user.DiscordUser
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.ws.{WSClient, WSRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * This class define helper to interface with discord OAuth API.
  *
  * @param ws       Http helper
  * @param agentUrl the agent url used in userAgent header.
  * @param version  the version of the client used in userAgent header.
  * @param clientId discord client id
  * @param secret   discord client secret
  */
class DiscordApi(val ws: WSClient, val agentUrl: String, val version: String,
                 val clientId: String,
                 val secret: String) {

  private val userAgent: String = s"DiscordBot ($agentUrl, $version)"
  private val discordHost = "https://discordapp.com/api"

  private val accessTokenPath = "/oauth2/token"
  private val authorizePath = "/oauth2/authorize"
  private val requestUserPath = "/users/@me"

  val OAuth2Helper = new OAuth2(clientId, secret, userAgent)

  /**
    * Step 1: redirect user to discord Oauth2 authentication page<br/>
    * Return the authorize url.
    *
    * @see [[OAuth2Helper.buildAuthorizeParam()]] an helper to build authorize params
    */
  val authorizeUrl: String = {
    discordHost + authorizePath
  }

  /**
    * Step 2: server to server call that give access to user token <br/>
    * Build access token request and send it to discord.
    *
    * @param code        [[String]]:     the code given buy OAuth2 provider
    * @param redirectUrl [[String]]: have to be the same provided in [[OAuth2.buildAuthorizeParam()]]
    * @param f           function ([[WSRequest]] => [[WSRequest]]) default to identity. Can be use to customize request
    * @return the access token
    */
  @throws(classOf[RequestFailedException]) // when request failed
  def callAccessToken(code: String, redirectUrl: String,
                      f: (WSRequest => WSRequest) = it => it): Future[AccessToken] = {
    val params: Map[String, Seq[String]] = Map(
      "grant_type" -> Seq("authorization_code"),
      "code" -> Seq(code),
      "redirect_uri" -> Seq(redirectUrl),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(secret)
    )
    val tokenUrl = discordHost + accessTokenPath
    val request = ws.url(tokenUrl).addHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    val response = f(request).post(params)
    response map {
      resp => {
        resp.status match {
          case 200 => val json = resp.json
            val accessToken: AccessToken = OAuth2Helper.parseAccessToken(json)
            accessToken
          case s =>
            throw RequestFailedException(s, request.url, resp.body)
        }
      }
    }
  }

  /**
    * Call discord to retrieve user data
    *
    * @param accessToken token returned by provider
    * @throws RequestFailedException
    */
  @throws(classOf[RequestFailedException]) // when request failed
  def getDiscordUser(accessToken: AccessToken): Future[DiscordUser] = {
    implicit val discordUserReader: Reads[DiscordUser] = (
      (JsPath \ "id").read[String] and
        (JsPath \ "username").read[String] and
        (JsPath \ "discriminator").read[String] and
        (JsPath \ "avatar").readNullable[String] and
        (JsPath \ "bot").readNullable[Boolean] and
        (JsPath \ "mfa_enabled").read[Boolean] and
        (JsPath \ "verified").readNullable[Boolean] and
        (JsPath \ "email").readNullable[String]
      ) (DiscordUser.apply _)

    val token: Token = accessToken.tokenType match {
      case BearerToken.TYPE => new BearerToken(accessToken.accessToken)
      case BotToken.TYPE => new BotToken(accessToken.accessToken)
      case _ => throw new IllegalArgumentException(s"Token of type ${accessToken.tokenType} is unknown")
    }
    val request = OAuth2Helper.includeHeaders(ws.url(discordHost + requestUserPath), token)
    request.get() map {
      resp => {
        resp.status match {
          case 200 => val user = discordUserReader.reads(resp.json).asOpt
            user match {
              case Some(u) => u
              case None => throw RequestFailedException(resp.status, request.url,
                s"Failed to parse ${resp.json} as valid DiscordUser")
            }
          case s => throw RequestFailedException(s, request.url, resp.body)
        }
      }
    }
  }

}
