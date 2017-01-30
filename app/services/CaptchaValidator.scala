package services

import javax.inject.Inject

import play.api.Logger
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.Future

/**
  * An helper to validate captcha produced by recaptcha
  */
class CaptchaValidator @Inject()(ws: WSClient) {

  case class RecaptchaResponse(success: Boolean, challenge_ts: String, hostname: String,
                               errorCodes: Option[Seq[String]])


  private val googleVerifyUrl: String = "https://www.google.com/recaptcha/api/siteverify"
  private val SECRET = "secret"
  private val RESPONSE = "response"
  private val REMOTEIP = "remoteip"

  implicit val captchaRead: Reads[RecaptchaResponse] = (
    (JsPath \ "success").read[Boolean] and
      (JsPath \ "challenge_ts").read[String] and
      (JsPath \ "hostname").read[String] and
      (JsPath \ "error-codes").readNullable[Seq[String]]
    ) (RecaptchaResponse.apply _)


  /**
    * Validate the captcha on google side
    *
    * @param value captcha value
    * @param ip    use ip
    * @return true if captcha is validate, false otherwise
    */
  def validate(value: String, ip: String): Future[Boolean] = {

    import scala.concurrent.ExecutionContext.Implicits.global
    Logger.info(s"Validating recaptcha from $ip")
    ws.url(googleVerifyUrl)
      .post(Map((SECRET) -> Seq(RecaptchaHelper.secret()), (RESPONSE) -> Seq(value), (REMOTEIP) -> Seq(ip)))
      .map { response => {
        val googleResp = response.json.as[RecaptchaResponse]
        Logger.info(s"Recaptcha validation response: $googleResp")
        googleResp.success
      }
      }
  }
}
