package controllers.forum

import javax.inject.Inject

import controllers.FlashConstant
import controllers.composition.Authenticated
import play.api.Logger
import play.api.db.Database
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Controller
import services.IdEncryptionUtil
import services.intern.database.{Forum, Role}

/**
  * Controller that handle forum pages. Restrict display according to connected user right on category.
  */
class ForumController @Inject()(db: Database, Auth: Authenticated, implicit val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  /**
    * "Index" displays root category
    */
  def index() = Auth {
    implicit request => {
      db.withConnection { connection =>
        val categories: Seq[Category] = request.user match {
          case Some(u) => Forum.getRootCategories(u)(connection)
          case None => Forum.getPublicCategories()(connection)
        }
        Ok(views.html.forum.index(request.user, categories))
      }
    }
  }

  /**
    * Display the category with external id given in parameter. Display sub categories of this category as well all
    * topic belongs to it.
    *
    * @param id
    * @return
    */
  def category(id: String) = Auth {
    implicit request => {
      Logger.debug(s"Category: $id => ${IdEncryptionUtil.decodeLong(id)}")
      db.withConnection {
        connection =>
          val category: Option[Category] = Forum.getCategory(IdEncryptionUtil.decodeLong(id), request.user)(connection)
          category.fold(Ok(views.html.forum.index(request.user, Seq()))) { cat =>
            Ok(views.html.forum.index(request.user, Seq(cat)))
          }
      }
    }
  }


  /**
    * Display the matrix of categories with right.
    * Require admin right.
    */
  def listJoinCategoryRole() = Auth {
    implicit request => {
      request.user match {
        case Some(u) =>
          if (u.admin) {
            db.withConnection {
              implicit conn =>
                val roles: Seq[Role] = Role.getAll()
                val categories: Seq[services.intern.database.forum.Category] = Forum.getCategories()
                val cats = Forum.getCategoriesWithAssociateRoles()
                val result = for (cat <- categories; role <- roles)
                  yield (cat, role, cats.exists(catRole => catRole.ca_id == cat.ca_id && catRole.ri_id == role.id))
                Ok(views.html.forum.admin.associateCategoryToRole(request.user, categories, roles, result))
            }
          } else {
            Logger.debug(s"User ${request.user} is not admin and cannot have access to resource ${
              controllers.forum.routes.ForumController.listJoinCategoryRole().url
            }")
            Redirect(controllers.routes.HomeController.index()).flashing(FlashConstant.MISSING_PERMISSION -> messagesApi
              .apply("flash.missing.right",
                controllers.forum.routes.ForumController.listJoinCategoryRole().url))
          }
        // No user, lets redirect to login page with requested url in flash
        case None => Redirect(controllers.authentication.routes.AuthenticationController.login()).flashing(
          FlashConstant.REQUESTED_RESOURCE -> controllers.forum.routes.ForumController.listJoinCategoryRole().url)
      }
    }
  }
}
