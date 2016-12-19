package api.authentication

import api.Response


case class LoginResponse(val code: Int, token: String) extends Response {

}
