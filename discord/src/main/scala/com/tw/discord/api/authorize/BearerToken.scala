package com.tw.discord.api.authorize

class BearerToken(token: String) extends Token {

  override val tokenType: String = "Bearer"
  override val value: String = token
}
