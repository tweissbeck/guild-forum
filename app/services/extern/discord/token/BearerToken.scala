package services.extern.discord.token

/**
 * Created by Thierry on 20/08/2016.
 */
class BearerToken(token: String) extends Token {

  override val tokenType: String = "Bearer"
  override val value: String = token
}
