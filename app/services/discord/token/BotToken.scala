package services.discord.token

/**
 * Created by Thierry on 20/08/2016.
 */
class BotToken(token: String) extends Token {
  override val value: String = token
  override val tokenType: String = "Bot"


}
