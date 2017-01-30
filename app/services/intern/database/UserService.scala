package services.intern.database

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._
import forms.SignInForm
import services.{Password, Salt}

import scala.util.Random


object UserService {

  private val SALT_KEY = "salt"
  val userParser = get[Long]("cl_id") ~ get[String]("cl_lastName") ~ get[String]("cl_firstName") ~
    get[String]("cl_mail") ~ get[Option[String]]("cl_login") ~ get[LocalDateTime]("cl_createdAt") ~
    get[Option[LocalDateTime]]("cl_lastLogin") ~ get[Boolean]("cl_admin") ~ get[String]("cl_password") ~
    get[String]("cl_salt") map {
    case id ~ name ~ firstName ~ mail ~ login ~ createdAt ~ lastLogin ~ admin ~ password ~ salt => {
      if (admin) {
        AdminUser(id, login, firstName, name, mail, createdAt, lastLogin, password, salt)
      } else {
        CommonUser(id, login, firstName, name, mail, createdAt, lastLogin, password, salt)
      }
    }

  }
  /** Table name */
  private val USER = "Client"

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
        WHERE cl_login = {value} OR cl_mail = {value}
      """
    SQL(query).on("value" -> value).as(userParser.singleOpt)
  }

  def findById(userId: Long)(implicit c: Connection): Option[User] = {
    val query =
      s"""
        SELECT * FROM $USER
        WHERE cl_id = {id}
      """
    SQL(query).on("id" -> userId).as(userParser.singleOpt)
  }

  def createUser(data: SignInForm, admin: Boolean = false)(implicit c: Connection): User = {
    val createdAt = LocalDateTime.now()
    val salt = Random.alphanumeric.take(50).mkString
    val encryptedSalt = Salt.encrypt(salt, SALT_KEY)
    val hashPassword = Password.hash(data.pwd, salt)
    val query =
      s"""
        INSERT INTO $USER
        (cl_firstName, cl_lastName, cl_login, cl_mail, cl_password, cl_salt, cl_createdAt, cl_admin)
        VALUES
        ({lastName}, {firstName}, {login}, {mail}, {password}, {salt}, {createdAt}, {admin})
      """
    val userId: Option[Long] = SQL(query).
      on(
        "lastName" -> data.lastName,
        "firstName" -> data.firstName,
        "salt" -> encryptedSalt,
        "password" -> hashPassword,
        "createdAt" -> createdAt,
        "login" -> data.login,
        "mail" -> data.mail,
        "admin" -> admin
      ).executeInsert()
    findById(userId.get).get
  }

  /**
    * Change the password of the user with login given as parameter. Call this in database transaction.
    *
    * @param login      user login
    * @param password   new password
    * @param connection implicit connection
    * @return
    */
  def changePassword(login: String, password: String)(implicit connection: Connection): Int = {
    val salt = Random.alphanumeric.take(50).mkString
    val encryptedSalt = Salt.encrypt(salt, SALT_KEY)
    val encryptedPassword = Password.hash(password, salt)
    val changePasswordQuery =
      s"""
         UPDATE $USER SET cl_password = {password} where cl_login = {login}
       """
    SQL(changePasswordQuery).on("password" -> encryptedPassword, "login" -> login).executeUpdate()
    val changeSaltQuery =
      s"""
         UPDATE $USER SET cl_salt = {salt} where cl_login = {login}
       """
    SQL(changeSaltQuery).on("salt" -> encryptedSalt, "login" -> login).executeUpdate()
  }
}





