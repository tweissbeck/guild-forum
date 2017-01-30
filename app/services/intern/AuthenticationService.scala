package services.intern

import java.sql.Connection

import play.api.Logger
import services.Password
import services.intern.database.{User, UserService}


object AuthenticationService {

  def authenticateUser(login: String, pwd: String)(implicit c: Connection): Option[User] = {
    val client: Option[User] = UserService.findByLoginOrMail(login)
    client match {
      case Some(c) =>
        if (Password.checkPassword(pwd, c))
          Some(c)
        else {
          Logger.info(s"Password does not match. Login:$login")
          None
        }
      case None =>
        Logger.info(s"No user with login or mail: $login")
        None
    }
  }

}
