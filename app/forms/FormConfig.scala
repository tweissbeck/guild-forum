package forms

import forms.config.RecaptchaConfig
import services.extern.captcha.RecaptchaHelper

/**
  * This bean is used to handle form configuration that will affect the render of form in views.html files.
  *
  * @param recaptchaConfig [[RecaptchaConfig]] the recaptcha configuration for the current form
  */
class FormConfig(val recaptchaConfig: RecaptchaConfig) {

}

object FormConfig {
  def apply(): FormConfig = {
    val recaptchaConfig: RecaptchaConfig = new RecaptchaConfig(RecaptchaHelper.enabled(),
      Some(RecaptchaHelper.public()))
    new FormConfig(recaptchaConfig)
  }
}
