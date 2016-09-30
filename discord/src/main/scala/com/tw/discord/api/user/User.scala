package com.tw.discord.api.user

/**
  * Users in Discord are generally considered the base entity. Users can spawn across the entire platform,
  * be members of guilds, participate and text and voice chat, and much more.
  * Users are separated by a distinction of "bot" vs "normal", although similar,
  * bot users are automated users that are "owned" by other users. Unlike normal users,
  * bot users do not have a limitation on the number of Guilds they can be a part of.
  *
  * @param id            the user's id, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param username      the user's username, not unique across the platform, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param discriminator the user's 4-digit discord-tag, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param avatar        the user's avatar hash, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param bot           whether the user belongs to a OAuth2 application, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param mfa_enabled   whether the user has two factor enabled on their account, require [[com.tw.discord.api.user.OAuth2DiscordScopes.IDENTITY]]
  * @param verified      whether the email on this account has been verified, require [[com.tw.discord.api.user.OAuth2DiscordScopes.EMAIL]]
  * @param email         the user's email, require [[com.tw.discord.api.user.OAuth2DiscordScopes.EMAIL]]
  */
case class User(id: String, username: String, discriminator: String, avatar: String, bot: Boolean, mfa_enabled: Boolean,
                verified: Option[Boolean], email: Option[String]) {

}
