package com.tw.discord.api

/**
  * Discord api entry point
  */
class DiscordApi(val clientId: String, val secret: String) {

  val userAgentUrl: String = "tw-scala-discord"
  val userAgentVersion: String = "1.0.0-SNAPSHOT"
  val userAgent: String = s"DiscordBot ($userAgentUrl, $userAgentVersion)"
  val discordHost = "https://discordapp.com/api"

  val userPath = "users"
  val accessTokenPath = "oauth2/token"
  val authorizePath = "oauth2/authorize"

}
