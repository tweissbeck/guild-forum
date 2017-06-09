package controllers.forum

import javax.inject.Inject

import controllers.composition.Authenticated
import play.api.Logger
import play.api.db.Database
import play.api.i18n.MessagesApi
import play.api.mvc.Controller
import services.IdEncryptionUtil
import services.intern.database.{Forum, Role}

/**
  * Controller that handle forum pages. Restrict display according to connected user right on category.
  */
class ForumController @Inject()(db: Database, Auth: Authenticated, implicit val messagesApi: MessagesApi)
  extends Controller {

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

  def listJoinCategoryRole() = Auth {
    implicit request => {
      request.user match {
        case Some(u) =>
          if (u.admin) {
            db.withConnection {
              implicit conn =>
                val roles = Role.getAll()
                val categories = Forum.getCategories()
                val cats = Forum.getCategoriesWithAssociateRoles()
                val result = for (cat <- categories; role <- roles)
                  yield (cat, role, cats.exists(catRole => catRole.catId == cat.id && catRole.roleId == role.id))
                Ok(views.html.forum.admin.associateCategoryToRole(request.user, result))
                ???
            }
          } else {
            ???
          }
        case None => ???
      }
    }
  }
}
