package controllers

import javax.inject.Inject

import forms.FormConfig
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Created by tweissbeck on 24/01/2017.
  */
class JoinUsController @Inject()(implicit val messagesApi: MessagesApi) extends Controller with I18nSupport {
  def get() = Action {
    implicit request =>
      Ok(views.html.joinUs.form(FormConfig()))
  }

  def post() = Action {
    implicit request =>
      Ok("OK")
  }
}
