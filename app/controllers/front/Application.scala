package controllers.front

import java.time.LocalDateTime

/**
  * @author tweissbeck
  */
case class Application(id: Long, subject: String, status: String, createDate: LocalDateTime, userId: Long,
                       userIdentifier: String)
  extends DisplayableDto(id) {

}
