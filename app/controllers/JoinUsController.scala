package controllers

import javax.inject.Inject

import forms.{ApplyForm, FormConfig}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by tweissbeck on 24/01/2017.
  */
class JoinUsController @Inject()(implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val applyForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "age" -> number(min = 0, max = 100),
      "share" -> optional(text),
      "charName" -> nonEmptyText,
      "charArmory" -> nonEmptyText,
      "charStrengthAndWeakness" -> optional(text),
      "addons" -> optional(text),
      "uiLink" -> optional(text),
      "why" -> optional(text),
      "attempt" -> optional(text),
      "whyYou" -> optional(text),
      "charte" -> boolean.verifying("error.charte", value => true && value),
      "g-recaptcha-response" -> nonEmptyText
    )
    (ApplyForm.apply)(ApplyForm.unapply)
  )

  def get() = Action {
    implicit request =>
      Ok(views.html.joinUs.form(FormConfig(), applyForm))
  }

  def post() = Action {
    implicit request =>
      applyForm.bindFromRequest().fold(
        withErrors => {
          // binding failure, you retrieve the form containing errors:
          println(withErrors.error("name"))
          println(withErrors.error("age"))
          println(withErrors.error("share"))
          BadRequest(views.html.joinUs.form(FormConfig(), withErrors))
        },
        data => {
          Ok("Apply ok")
        }
      )
  }

  def list() = {

  }

  def show() = {

  }
}
