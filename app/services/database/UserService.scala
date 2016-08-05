package services.database

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._
import forms.SignInForm
import services.{Password, Salt}

import scala.util.Random


object UserService {


  val userParser = get[Long]("cl_id") ~ get[String]("cl_lastName") ~ get[String]("cl_firstName") ~
    get[String]("cl_mail") ~ get[Option[String]]("cl_login") ~ get[LocalDateTime]("cl_createAt") ~
    get[Option[LocalDateTime]]("cl_lastLogin") ~ get[Boolean]("cl_admin") ~ get[String]("cl_password") ~
    get[String]("cl_salt") map {
    case id ~ name ~ firstName ~ mail ~ login ~ createdAt ~ lastLogin ~ admin ~ password ~ salt =>
      User(id, login, firstName, name, mail, createdAt, lastLogin, admin, password, salt)
  }
  /* Table name*/
  private val USER = "Client";

  def list()(implicit c: Connection): Seq[User] = {
    val query =
      s"""
        SELECT * FROM
        $USER;
      """
    SQL(query).as(userParser.*)
  }

  def findByLogin(login: String)(implicit c: Connection): Option[User] = {
    val query =
      s"""
        SELECT * FROM $USER
        WHERE cl_login = {login};
      """
    SQL(query).on("login" -> login).as(userParser.singleOpt);
  }

  def findByLoginOrMail(value: String)(implicit c: Connection): Option[User] = {
    val query =
      s"""
        SELECT * FROM $USER
        WHERE cl_login = {value} OR cl_email = {value}
      """
    SQL(query).on("value" -> value).as(userParser.singleOpt)
  }

  def createUser(data: SignInForm)(implicit c: Connection): User = {
    val createdAt = LocalDateTime.now()
    val salt = Random.alphanumeric.take(50).mkString
    val encryptedSalt = Salt.encrypt(salt, "salt")
    val hashPassword = Password.hash(data.pwd, salt)
    val query =
      s"""
        INSERT INTO $USER
        (cl_firstName, cl_lastName, cl_login, cl_mail, cl_password, cl_salt, cl_alias, cl_createdAt)
        VALUE
        ({lastName}, {firstName}, {login}, {mail}, {password}, {salt}, {alias})
      """
    val userId: Option[Long] = SQL(query).on("lastName" -> data.lastName,
      "fistName" -> data.firstName,
      "salt" -> encryptedSalt,
      "password" -> hashPassword).executeInsert()
    ???
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
 * @param lastLogin last user's login date
 * @param admin     true if user as admin right
 * @param password  hashed password (with salt)
 * @param salt      encrypted salt
 */
case class User(id: Long, login: Option[String], firstName: String, lastName: String, mail: String, createdAt: LocalDateTime,
                lastLogin: Option[LocalDateTime], admin: Boolean, password: String, salt: String) {
}



