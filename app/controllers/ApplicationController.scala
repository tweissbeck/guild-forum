package controllers

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import controllers.composition.{AdminAction, Authenticated}
import forms.{ApplyForm, FormConfig}
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, Controller}
import services.IdEncryptionUtil
import services.extern.captcha.CaptchaValidator
import services.intern.ApplicationService
import services.intern.database.{Application, ApplicationDetail, ApplicationWithUser}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Controller for application.
  *
  * @author tweissbeck
  */
class ApplicationController @Inject()(db: Database, Auth: Authenticated, Admin: AdminAction,
                                      captchaValidator: CaptchaValidator,
                                      implicit val messageApi: MessagesApi) extends Controller {

  /**
    * Convert an [[ApplicationWithUser]] into an [[controllers.front.Application]]
    */
  private def convert(applicationWithUser: ApplicationWithUser): controllers.front.Application = {
    def buildUser(): String = {
      applicationWithUser.user.cl_firstName + " " + applicationWithUser.user.cl_LastName
    }

    def buildSubject(): String = {
      "TODO generate subject"
    }

    controllers.front.Application(applicationWithUser.ap_id.get, buildSubject(), applicationWithUser.ap_status,
      applicationWithUser.ap_creationDate, applicationWithUser.user.cl_id, buildUser())
  }

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

  /**
    * List current application
    *
    * @return
    */
  def list() = Admin { implicit request =>
    db.withConnection {
      implicit connection =>
        val currentApplication: Seq[ApplicationWithUser] = ApplicationService.getNew()
        Ok(views.html.application.admin.applicationList(request.admin, currentApplication.map(e => convert(e))))
    }

  }

  /**
    * Show details of an application
    *
    * @param id
    * @return
    */
  def detail(id: String) = Admin {
    implicit request =>
      db.withConnection {
        implicit conn => {
          val applicationPK: Long = IdEncryptionUtil.decodeLong(id)
          val application = ApplicationService.findById(applicationPK)
          if (application.isDefined) {
            Ok(views.html.application.admin.detail(request.admin, convert(application.get)))
          } else {
            NotFound(views.html.commons.ResourceNotFound(Some(request.admin), messageApi.apply("application.resource"),
              Some(String.valueOf(applicationPK))))
          }
        }
      }
  }


  /**
    * Diplay the application form
    *
    * @return
    */
  def get() = Action {
    implicit request =>
      Ok(views.html.application.form(FormConfig(), applyForm))
  }

  /**
    * Handle the application form submission
    *
    * @return
    */
  def post() = Auth {
    implicit request =>
      if (request.user.isDefined) {
        val user = request.user.get
        applyForm.bindFromRequest().fold(
          withErrors => {
            // binding failure, you retrieve the form containing errors
            BadRequest(views.html.application.form(FormConfig(), withErrors))
          },
          data => {
            db.withConnection {
              implicit connection =>
                if (Await
                  .result(captchaValidator.validate(data.recaptcha, request.remoteAddress),
                    Duration(3, TimeUnit.SECONDS))) {
                  val application = ApplicationDetail(None, Application.STATUS_NEW, LocalDateTime.now(), "")
                  val applicationId: Long = ApplicationService.insert(application, user.id)
                  Logger.debug(s"Application $applicationId successfully created")
                  Redirect(controllers.routes.ApplicationController.detail(IdEncryptionUtil.encode(applicationId)))
                } else {
                  InternalServerError("Bot!")
                }
            }
          })
      } else {
        // No user, redirect to login page
        Redirect(controllers.authentication.routes.AuthenticationController.login())
          .flashing(FlashConstant.REQUESTED_RESOURCE -> controllers.routes.ApplicationController.get().url);
      }
  }

}
