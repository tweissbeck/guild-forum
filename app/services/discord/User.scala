package services.discord

/**
  * Discord User <br/>
  * <a href="https://discordapp.com/developers/docs/resources/user#user-structure">https://discordapp.com/developers/docs/resources/user#user-structure</a>
  */
case class User(id: String, username: String, discriminator: String, avatar: String, bot: Boolean, mfaEnabled: Boolean,
                verified: Boolean, email: String) {

}
