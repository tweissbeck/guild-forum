package controllers

import javax.inject._

import business.recruitment.RecruitmentStatus
import controllers.composition.Authenticated
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._

@Singleton
class HomeController @Inject()(implicit val messagesApi: MessagesApi, Auth: Authenticated)
  extends Controller with I18nSupport {

  def index = Auth { implicit request =>
    def getRecruitmentStatus(): Seq[RecruitmentStatus] = {
      Seq(new RecruitmentStatus("Guerrier", "war", Map("Fury" -> false, "Arm" -> false, "Protection" -> true)),
        new RecruitmentStatus("Mage", "mage", Map("Arcane" -> true)))
    }

    Ok(views.html.index(request.user, getRecruitmentStatus()))
  }


}
