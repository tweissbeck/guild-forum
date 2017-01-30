package services

import com.typesafe.config.ConfigFactory

/**
  * An helper to access recaptcha config
  */
object RecaptchaHelper {

  private val _enabled = ConfigFactory.load().getBoolean("recaptcha.enabled")
  private var _secret: String = ""
  private var _public: String = ""
  if (_enabled) {
    _secret = ConfigFactory.load().getString("recaptcha.private")
    _public = ConfigFactory.load().getString("recaptcha.public")
  }

  def enabled(): Boolean = {
    _enabled
  }

  def secret(): String = {
    _secret
  }

  def public(): String = {
    _public
  }

}
