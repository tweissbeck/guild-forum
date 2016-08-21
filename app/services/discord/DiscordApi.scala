package services.discord

import play.api.libs.ws._
import services.discord.token.Token

/**
 * Created by Thierry on 20/08/2016.
 */
trait DiscordApi {

  val ws: WSClient
  val host = "https://discordapp.com/api"


  def getUserRequest(token: Token): WSRequest = {
    val url = host + "/users/{@me}"
    ws.url(url).withHeaders("Authorization" -> token.toString)
  }

}
