package services.database

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._


object UserService {

  /* Table name*/
  private val USER = "Client";

  val userParser = get[Long]("cl_id") ~ get[String]("cl_lastName") ~ get[String]("cl_firstName") ~
    get[String]("cl_mail") ~ get[Option[String]]("cl_login") ~ get[LocalDateTime]("cl_createAt") ~
    get[Option[LocalDateTime]]("cl_lastLogin") ~ get[Boolean]("cl_admin") map {
    case id ~ name ~ firstName ~ mail ~ login ~ createdAt ~ lastLogin ~ admin =>
      User(id, login, firstName, name, mail, createdAt, lastLogin, admin)
  }

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
}


case class User(id: Long, login: Option[String], firstName: String, lastName: String, mail: String, createdAt: LocalDateTime,
                lastLogin: Option[LocalDateTime], admin: Boolean) {
}

