package forms

/**
  * Apply form
  */
case class ApplyForm(name: String, age: Int, share: Option[String], charName: String, charArmory: String,
                     charStrengthAndWeakness: Option[String], addons: Option[String], uiLink: Option[String],
                     why: Option[String], attempt: Option[String], whyYou: Option[String], charte: Boolean,
                     recaptcha: String) {
}


