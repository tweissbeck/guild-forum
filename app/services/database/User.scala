package services.database

import java.time.LocalDateTime

abstract class User(val id: Long, val login: Option[String], val firstName: String, val lastName: String,
                    val mail: String,
                    val createdAt: LocalDateTime, val lastLogin: Option[LocalDateTime], val admin: Boolean,
                    val password: String, val salt: String)


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