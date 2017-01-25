package forms

import com.typesafe.config.ConfigFactory
import forms.config.RecaptchaConfig

/**
  * This bean is used to handle form configuration that will affect the render of form in views.html files.
  *
  * @param recaptchaConfig [[RecaptchaConfig]] the recaptcha configuration for the current form
  */
class FormConfig(val recaptchaConfig: RecaptchaConfig) {

}

object FormConfig {
  def apply(): FormConfig = {
    val recaptchaConfigEnabled = ConfigFactory.load().getBoolean("recaptcha.enabled")

    val recaptchaConfig: RecaptchaConfig = new RecaptchaConfig(recaptchaConfigEnabled,
      if (recaptchaConfigEnabled) Some(ConfigFactory.load().getString("recaptcha.public")) else None)
    new FormConfig(recaptchaConfig)
  }
}
