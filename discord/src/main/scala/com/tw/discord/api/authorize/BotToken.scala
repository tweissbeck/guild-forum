package com.tw.discord.api.authorize

class BotToken(token: String) extends Token {
  override val value: String = token
  override val tokenType: String = "Bot"
}
