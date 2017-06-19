package controllers.front

import java.time.LocalDateTime

import services.intern.database.User

/**
  * [[services.intern.database.User]] representation for list page
  *
  * @author tweissbeck
  */
case class UserList(id: Long, login: Option[String], firstName: String, lastName: String,
                    mail: String,
                    createdAt: LocalDateTime, lastLogin: Option[LocalDateTime], admin: Boolean,
                    password: String, salt: String) extends DisplayableDto(id) {

  def this(user: User) {
    this(user.id, user.login, user.firstName, user.lastName, user.mail, user.createdAt, user.lastLogin, user.admin,
      user.password, user.salt)
  }

}
