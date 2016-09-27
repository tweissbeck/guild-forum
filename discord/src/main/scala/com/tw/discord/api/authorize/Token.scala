package com.tw.discord.api.authorize

trait Token {
  val tokenType: String
  val value: String

  override def toString: String = s"$tokenType $value"

}
