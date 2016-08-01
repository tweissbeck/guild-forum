package services

import java.sql.Connection

import services.database.{Client, ClientService}


object AuthenticationService {

  def authenticateUser(login: String, pwd: String)(implicit c: Connection): Option[Client] = {
    val client: Option[Client] = ClientService.findByLogin(login)
    client match {
      case Some(c) =>
        if ("tweissbeck".equals(login) && ".123.".equals(pwd)) Some(c) else None
      case None => client
    }
  }

}
