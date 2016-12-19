package api

/**
  * Created by tweissbeck on 02/11/2016.
  */
case class ErrorResponse(val code: Int, error: String, detail: Option[String] = None) extends Response {
  def this(code: Int, error: String, detail: String) = {
    this(code, error, Some(detail))
  }
}
