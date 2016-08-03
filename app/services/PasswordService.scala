package services

import java.security.MessageDigest

import play.api.libs.Codecs
import services.database.User

class Password(user: User) {

  /**
   * Check the user password with data base
   *
   * @return true if the password marches the user password, false otherwise
   */
  def checkPassword(userPassword: String): Boolean = {
    hash(userPassword).equals(this.user.password)
  }

  /**
   * Generate the password hash with salt mechanism
   *
   * @return the hashed password
   */
  def hash(password: String): String = {
    val messageDigest = MessageDigest.getInstance("SHA-256");
    val pwdWithSalt = user.salt + user.id + password
    Codecs.toHexString(messageDigest.digest(pwdWithSalt.getBytes("UTF-8")))
  }
}
