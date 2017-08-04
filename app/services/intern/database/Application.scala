package services.intern.database

import java.time.LocalDateTime

trait ApplicationTrait {

  val ap_id: Option[Long]
  val ap_status: String
  val ap_creationDate: LocalDateTime
}

/**
  * @author tweissbeck
  */
case class Application(ap_id: Option[Long], ap_status: String, ap_creationDate: LocalDateTime)
  extends ApplicationTrait {

}

case class ApplicationDetail(ap_id: Option[Long], override val ap_status: String,
                             override val ap_creationDate: LocalDateTime, data: String)
  extends ApplicationTrait()

case class ApplicationWithUser(ap_id: Option[Long], ap_status: String, ap_creationDate: LocalDateTime, ap_data: String,
                               user: ApplicationUser)

case class ApplicationUser(cl_id: Long, cl_firstName: String, cl_LastName: String, cl_mail: String) {

}

object Application {
  val STATUS_NEW = "NEW"
  val STATUS_IN_PROGRESS = "IN_PROGRESS"
  val STATUS_VALIDATE = "VALIDATE"
  val STATUS_REFUSED = "REFUSED"


  def TABLE_NAME: String = "Application"

  def STATUS: String = "ap_status"

  def ID: String = "ap_id"

  def CREATION_DATE: String = "ap_creationDate"

  def DATA: String = "ap_data"

  def USER: String = "ap_user"
}
