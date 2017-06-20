package api

/**
  * Created by tweissbeck on 02/11/2016.
  */
trait Response {
  val code: Int
}

object Response {
  val OK = new Response {
    override val code: Int = 200
  }
}
