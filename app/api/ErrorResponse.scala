package api

/**
  * Created by tweissbeck on 02/11/2016.
  */
case class ErrorResponse(val code: Int, error: String, detail: Option[String] = None) extends Response {
  def this(code: Int, error: String, detail: String) = {
    this(code, error, Some(detail))
  }
}

object ErrorResponse {
  val USER_SELF_DELETE: String = "ERROR_USER_CANT_DELETE_SELF"
  val DELETE_USER_FAILED: String = "ERROR_USER_DELETE_FAILED"
  val USER_NOT_EXIST: String = "USER_NOT_EXIST"
}
