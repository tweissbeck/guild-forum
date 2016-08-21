package services.discord.token

/**
 * Created by Thierry on 20/08/2016.
 */
trait Token {
  val tokenType: String
  val value: String

  override def toString: String = s"$tokenType value"

}
