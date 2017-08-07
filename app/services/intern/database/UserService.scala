package services.intern.database

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._
import com.tw.discord.api.user.DiscordUser
import forms.SignInForm
import services.{Password, Salt}

import scala.util.Random


object UserService {


  private val SALT_KEY = "salt"
  private val userParser = get[Long]("cl_id") ~ get[String]("cl_lastName") ~ get[String]("cl_firstName") ~
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

  /**
    * Return list of users
    *
    * @param c sql connection
    * @return all users
    */
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

  /**
    * Find user by its login or mail
    *
    * @param value login or mail
    * @param c     sql connection
    * @return the user that match the login or email or None if none match
    */
  def findByLoginOrMail(value: String)(implicit c: Connection): Option[User] = {
    val query =
      s"""
        SELECT * FROM $USER
        WHERE cl_login = {value} OR cl_mail = {value}
      """
    SQL(query).on("value" -> value).as(userParser.singleOpt)
  }

  /**
    * Find user by its primary key
    *
    * @param userId user primary key
    * @param c      sql connection
    * @return the user that matches the id or None
    */
  def findById(userId: Long)(implicit c: Connection): Option[User] = {
    val query =
      s"""
        SELECT * FROM $USER
        WHERE cl_id = {id}
      """
    SQL(query).on("id" -> userId).as(userParser.singleOpt)
  }

  /**
    * Create new user from discord credentials
    *
    * @param user       user data provided by discord.
    * @param password   user password. This value will be encoded before persist
    * @param connection sql connection
    * @return the created user
    */
  def createUser(user: DiscordUser, password: String)(implicit connection: Connection): User = {
    val createdAt = LocalDateTime.now()
    val salt = Random.alphanumeric.take(50).mkString
    val encryptedSalt = Salt.encrypt(salt, SALT_KEY)
    val hashPassword = Password.hash(password, salt)
    val query =
      s"""
        INSERT INTO $USER
        (cl_firstName, cl_lastName, cl_login, cl_mail, cl_password, cl_salt, cl_createdAt, cl_admin)
        VALUES
        ({lastName}, {firstName}, {login}, {mail}, {password}, {salt}, {createdAt}, {admin})
      """
    val userId: Option[Long] = SQL(query).
      on(
        "lastName" -> "",
        "firstName" -> "",
        "salt" -> encryptedSalt,
        "password" -> hashPassword,
        "createdAt" -> createdAt,
        "login" -> (user.username + '#' + user.discriminator),
        "mail" -> user.email.get,
        "admin" -> false
      ).executeInsert()
    findById(userId.get).get
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


  /**
    * Delete user with id given in parameter
    *
    * @param id         user primary key
    * @param connection sql connection
    */
  def delete(id: Long)(implicit connection: Connection): Unit = {
    SQL(s"DELETE FROM $USER WHERE cl_id = {id}").on("id" -> id).execute()
  }

  /**
    * Update User entiry
    *
    * @param id         primary key of the user
    * @param diffs      list of diff as a map of updated columns where keys are columns name and values the updated values.
    * @param connection sql connection
    * @return
    */
  def update(id: Long, diffs: Map[String, String])(implicit connection: Connection): Int = {
    if (diffs.nonEmpty) {
      val params = diffs.map(e => new NamedParameter(e._1, e._2))
      val paramsAsString = "SET " + diffs.map(e => s" ${e._1} = {${e._1}}").mkString(", ")
      val query = SQL(
        s"""
        UPDATE $USER $paramsAsString WHERE ${User.ID} = {id}
      """)

        /**
          * use the *seq like* to varargs special notation
          *
          * @see chapter 4.6.x [repeated parameter]
          */
        .on(params.toSeq: _*).on("id" -> id)
      query.executeUpdate()
    } else {
      0
    }
  }
}