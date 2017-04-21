package com.tw.discord.api.authorize

class BearerToken(token: String) extends Token {

  override val tokenType: String = BearerToken.TYPE;
  override val value: String = token
}

object BearerToken {
  val TYPE: String = "Bearer"
}


