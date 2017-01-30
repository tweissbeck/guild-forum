package initialization

import javax.inject.{Inject, Singleton}

import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.db.Database
import services.intern.database.UserService

/**
  * Created by tweissbeck on 05/10/2016.
  */
trait Init

@Singleton
class InitImpl @Inject()(db: Database) extends Init {
  init()

  def init(): Unit = {
    val env = ConfigFactory.load().getString("env.name")
    // change password of admin user
    if (env.equals("DEV")) {
      Logger.info("Update password to 'admin' user to '123'")
      db.withTransaction { implicit connection =>
        if (UserService.changePassword("admin", "123") != 1) {
          Logger.error("Failed to update user password")
        }
      }
    }
  }
}
