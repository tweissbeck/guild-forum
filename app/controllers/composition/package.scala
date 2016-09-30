package controllers

import play.api.Logger
import play.api.mvc.Request


package object composition {
  val AUTHENTICATION_TOKEN_KEY = AuthenticationCookie.NAME
  val AUTHENTICATION_HEADER_KEY = "AuthenticationToken"

  def getUserFromRequest[A](request: Request[A]): Option[Long] = {

    def fromCookie(): Option[Long] = {
      val cookie = request.cookies.get(AUTHENTICATION_TOKEN_KEY);
      cookie match {
        case Some(c) => {
          try {
            JWT.validateJWT(c.value)
          }
          catch {
            case e: Exception => {
              Logger.error(s"Fail to parse cookie: ${c.value}", e)
              None
            }
          }
        }
        case None => None
      }
    }

    def fromHeader(): Option[Long] = {
      val header = request.headers.get(AUTHENTICATION_HEADER_KEY)
      try {
        header.fold(None[Long])(value => Some(value.toLong))
      } catch {
        case e: NumberFormatException => None
      }
    }
    fromCookie().orElse(fromHeader())
  }


}
