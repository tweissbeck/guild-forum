package services

import java.security.MessageDigest

import play.api.libs.Codecs
import services.database.User


object Salt {
  def encrypt(salt: String, keyAlias: String): String = ???

  def decrypt(encryptedSalt: String, keyAlias: String): String = ???
}

object Password {
  /**
   * Check the user password
   *
   * @return true if the password marches the user password, false otherwise
   */
  def checkPassword(userPassword: String, user: User): Boolean = {
    hash(userPassword, user.salt).equals(user.password)
  }

  /**
   * Generate the password hash with salt mechanism
   *
   * @return the hashed password
   */
  def hash(password: String, salt: String): String = {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val passwordWithSalt = salt + password
    Codecs.toHexString(messageDigest.digest(passwordWithSalt.getBytes("UTF-8")))
  }
}
