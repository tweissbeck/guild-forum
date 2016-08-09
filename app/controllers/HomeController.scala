package controllers

import javax.inject._

import controllers.composition.Authenticated
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

@Singleton
class HomeController @Inject()(implicit val messagesApi: MessagesApi, Auth: Authenticated) extends Controller with I18nSupport {

  def index = Auth { implicit request =>
    Ok(views.html.index(request.user))
  }


}
