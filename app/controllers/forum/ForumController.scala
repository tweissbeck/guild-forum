package controllers.forum

import javax.inject.Inject

import controllers.composition.Authenticated
import play.api.mvc.Controller

/**
  * Controller that handle forum pages
  */
class ForumController @Inject()(val Auth: Authenticated) extends Controller {

  def index() = Auth {
    implicit request => {
      Ok
    }
  }
}
