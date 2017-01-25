package forms.config

/**
  * Recaptcha configuration for forms
  *
  * @param recaptchaEnabled [[Boolean]] true if recaptcha div element should be added on forms
  * @param publicKey        [[scala.Option]] of [[String]] the recaptcha public key, must be [[scala.Some]] if recaptchaEnabled is set to true
  * @see https://www.google.com/recaptcha/
  */
class RecaptchaConfig(val recaptchaEnabled: Boolean, val publicKey: Option[String] = None) {
  if (recaptchaEnabled && publicKey.isEmpty) {
    throw new IllegalArgumentException("Recaptcha is enabled but the public key was not provided.")
  }
}
