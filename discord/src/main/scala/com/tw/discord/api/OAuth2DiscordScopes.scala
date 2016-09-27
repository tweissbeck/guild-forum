package com.tw.discord.api

/**
  * Scopes provide access to certain resources of a user's account. API client or service should only request scopes it requires for operation.
  */
object OAuth2DiscordScopes extends Enumeration {
  /**
    * allows /users/@me without email
    */
  val IDENTITY = Value("identity")
  /**
    * enables /users/@me to return an email
    */
  val EMAIL = Value("email")
  /**
    * allows /users/@me/connections to return linked Twitch and YouTube accounts
    */
  val CONNECTIONS = Value("connections")
  /**
    * allows /users/@me/guilds to return basic information about all of a user's guilds
    */
  val GUILDS = Value("guilds")
  /**
    * allows /invites/{invite.id} to be used for joining a user's guild
    */
  val GUILDS_JOIN = Value("guilds.join")

  /**
    * for oauth2 bots, this puts the bot in the user 's selected guild by default
    */
  val BOT = Value("bot")

}
