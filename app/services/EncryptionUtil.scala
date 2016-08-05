package services

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.{Cipher, SecretKey}
import javax.xml.bind.DatatypeConverter

import com.typesafe.config.ConfigFactory
import play.api.libs.Codecs
import services.database.User


object Salt {

  private val ALGO = "AES";

  /**
   * Get the secret key from the configuration
   *
   * @param keyAlias : key usage
   * @return
   */
  private def getKey(keyAlias: String): SecretKey = {
    val keyAsString = ConfigFactory.load().getString(s"crypto.key.$keyAlias")
    val keyData = DatatypeConverter.parseHexBinary(keyAsString)
    new SecretKeySpec(keyData, 0, keyData.length, ALGO)
  }

  def encrypt(salt: String, keyAlias: String): String = {
    val key = getKey(keyAlias)
    val c = Cipher.getInstance(ALGO)
    c.init(Cipher.ENCRYPT_MODE, key)
    val encrypted = c.doFinal(salt.getBytes("UTF-8"))
    DatatypeConverter.printHexBinary(encrypted)
  }

  def decrypt(encryptedSalt: String, keyAlias: String): String = {
    val key = getKey(keyAlias)
    val c = Cipher.getInstance(ALGO)
    c.init(Cipher.DECRYPT_MODE, key)
    val decrypted = c.doFinal(DatatypeConverter.parseHexBinary(encryptedSalt))
    new String(decrypted, "UTF-8")
  }
}

object Password {
  /**
   * Check the user password
   *
   * @return true if the password marches the user password, false otherwise
   */
  def checkPassword(userPassword: String, user: User): Boolean = {
    hash(userPassword, Salt.decrypt(user.salt, "salt")).equalsIgnoreCase(user.password)
  }

  /**
   * Generate the password hash with salt mechanism
   *
   * @return the hashed password
   */
  def hash(password: String, salt: String): String = {
    println(s"salt = $salt")
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val passwordWithSalt = s"$salt:$password"
    Codecs.toHexString(messageDigest.digest(passwordWithSalt.getBytes("UTF-8")))
  }
}
