package services.intern.database

import java.time.LocalDateTime

/**
  * @author tweissbeck
  */
case class Application(ap_id: Long, ap_status: String, ap_data: String, ap_creationDate: LocalDateTime) {

}

object Application {
  def TABLE_NAME: String = "Application"

  def STATUS: String = "ap_status"

  def ID: String = "ap_id"

  def CREATION_DATE: String = "ap_creationDate"

  def DATA: String = "ap_data"
}
