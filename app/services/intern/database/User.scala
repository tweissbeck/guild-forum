package services.intern.database

import java.time.LocalDateTime

import scala.collection.mutable

object User {

  val ID = "cl_id"
  val LOGIN = "cl_login"
  val LAST_NAME = "cl_lastName"
  val FIRST_NAME = "cl_firstName"
  val MAIL = "cl_mail"
  val ADMIN = "cl_admin"
  val CREATED_AT = "cl_createdAt"
  val LAST_LOGIN = "lastLogin"
  val PASSWORD = "cl_password"
  val SALT = "cl_salt"
}

abstract class User(val id: Long, val login: Option[String], val firstName: String, val lastName: String,
                    val mail: String,
                    val createdAt: LocalDateTime, val lastLogin: Option[LocalDateTime], val admin: Boolean,
                    val password: String, val salt: String) {


  def diff(other: User): Map[String, String] = {
    val map = new mutable.HashMap[String, String]()
    if (!other.password.isEmpty && !password.equals(other.password)) {
      map.put(User.PASSWORD, other.password)
    }

    if (other.login.isDefined && login.isDefined) {
      if (!login.get.equals(other.login.get)) {
        map.put(User.LOGIN, other.login.get)
      }
    }

    if (!mail.equals(other.mail)) {
      map.put(User.MAIL, other.mail)
    }

    if (!firstName.equals(other.firstName)) {
      map.put(User.FIRST_NAME, other.firstName)
    }

    if (!lastName.equals(other.lastName)) {
      map.put(User.LAST_NAME, other.lastName)
    }

    map.toMap
  }
}


/**
  * User with all database fields
  *
  * @param id        user data base id
  * @param login     user login, may be empty
  * @param firstName user first name
  * @param lastName  user last name
  * @param mail      user mail adress
  * @param createdAt user creation date
  * @param lastLogin last user's login datef
  * @param password  hashed password (with salt)
  * @param salt      encrypted salt
  */
case class CommonUser(override val id: Long, override val login: Option[String], override val firstName: String,
                      override val lastName: String, override val mail: String, override val createdAt: LocalDateTime,
                      override val lastLogin: Option[LocalDateTime], override val password: String,
                      override val salt: String)
  extends User(id, login, firstName, lastName, mail, createdAt, lastLogin, false, password, salt) {
}

case class AdminUser(override val id: Long, override val login: Option[String], override val firstName: String,
                     override val lastName: String, override val mail: String, override val createdAt: LocalDateTime,
                     override val lastLogin: Option[LocalDateTime], override val password: String,
                     override val salt: String)
  extends User(id, login, firstName, lastName, mail, createdAt, lastLogin, true, password, salt)