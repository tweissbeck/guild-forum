package services

import java.sql.Connection

import services.database.{User, UserService}


object AuthenticationService {

  def authenticateUser(login: String, pwd: String)(implicit c: Connection): Option[User] = {
    val client: Option[User] = UserService.findByLogin(login)
    client match {
      case Some(c) =>
        if ("tweissbeck".equals(login) && ".123.".equals(pwd)) Some(c) else None
      case None => client
    }
  }

}
