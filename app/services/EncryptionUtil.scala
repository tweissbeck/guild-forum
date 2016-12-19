package services

import java.security.{Key, MessageDigest}
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

import com.typesafe.config.ConfigFactory
import play.api.libs.Codecs
import services.database.User

/**
 * Define an helper to get a SecretKey from its alias and its algorithm
 */
trait KeyUser {
  /**
   * Get the secret key value from the configuration
   *
   * @param keyAlias : key usage
   * @return
   */
  protected def getKey(keyAlias: String)(implicit algo: String): Key = {
    val keyAsString = ConfigFactory.load().getString(s"crypto.key.$keyAlias")
    val keyData = DatatypeConverter.parseHexBinary(keyAsString)
    new SecretKeySpec(keyData, 0, keyData.length, algo)
  }

  /**
   *
   * @param mode cipher mode [[Cipher.DECRYPT_MODE]] or [[Cipher.ENCRYPT_MODE]]
   * @param key  the key to use in [[Cipher]]
   * @param data
   * @param transformInput
   * @param outputTransform
   * @param algo algorithm to use
   * @return
   */
  protected def cipher(mode: Int, key: Key, data: String,
                       transformInput: (String) => Array[Byte],
                       outputTransform: (Array[Byte]) => String)(implicit algo: String): String = {
    val c = Cipher.getInstance(algo)
    c.init(mode.toInt, key)
    outputTransform(c.doFinal(transformInput(data)))
  }
}

object Salt extends KeyUser {

  implicit private val ALGO = "AES";

  def encrypt(salt: String, keyAlias: String): String = {
    val key = getKey(keyAlias)
    this.cipher(Cipher.ENCRYPT_MODE, key, salt, (s) => s.getBytes("utf-8"), (s) => DatatypeConverter.printHexBinary(s))
  }

  def decrypt(encryptedSalt: String, keyAlias: String): String = {
    val key = getKey(keyAlias)
    this.cipher(Cipher.DECRYPT_MODE, key, encryptedSalt, (s) => DatatypeConverter.parseHexBinary(s), s => new String(s, "utf-8"))
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
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val passwordWithSalt = s"$salt:$password"
    Codecs.toHexString(messageDigest.digest(passwordWithSalt.getBytes("UTF-8")))
  }
}

/**
 * Helper to encode beans id like table identifier.
 */
object IdEncryptionUtil extends KeyUser {
  implicit val ALGO = "AES"
  val key = getKey("id")

  private def stringToByte: String => Array[Byte] = {
    s => s.getBytes("utf-8")
  }

  private def printBase64Binary: Array[Byte] => String = {
    s => DatatypeConverter.printBase64Binary(s)
  }

  private def byteToString: Array[Byte] => String = {
    s => new String(s, "utf-8")
  }

  private def parseBase64Binary: String => Array[Byte] = {
    s => DatatypeConverter.parseBase64Binary(s)
  }

  def decode[T](value: String, t: (String) => T): T = {
    t(this.cipher(Cipher.DECRYPT_MODE, key, value, parseBase64Binary, byteToString))
  }

  def encode(value: String): String = {
    this.cipher(Cipher.ENCRYPT_MODE, key, value, stringToByte, printBase64Binary)
  }

  def decode(value: String): String = {
    this.decode(value, x => identity(x))
  }

  def encode(value: Long): String = {
    this.encode(value.toString)
  }

  def decodeLong(value: String): Long = {
    this.decode(value, s => s.toLong)
  }
}
