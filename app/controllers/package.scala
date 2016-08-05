import play.api.mvc.Cookie
import services.database.User

/**
 * Created by tweissbeck on 05/08/2016.
 */
package object controllers {

  /**
   * Helper to build a cookie that contains authentication data
   *
   */
  // TODO use JWT to save data in cookie
  object AuthenticationCookie {
    val NAME = "token"

    def cookie(user: User): Cookie = Cookie(NAME, s"${user.id}", Some(16000), secure = false, httpOnly = true)
  }

}
