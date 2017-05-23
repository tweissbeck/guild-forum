package services.intern.database

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import controllers.forum.RootCategory
import play.api.Logger

/**
  * This object give access to Topic data.
  *
  * @author tweissbeck
  */
object Topic {

  private val rootCategoryParser: ResultSetParser[List[(String, Option[String], Option[String])]] = {
    (str("cat_label") ~ get[Option[String]]("childCat_label") ~ get[Option[String]]("topic_label") map
      (flatten)) *
  }

  def getPublic()(implicit connection: Connection): Seq[RootCategory] = {
    val sql =
      """
        SELECT
          c.ca_label AS cat_label,
          childCategory.ca_label AS childCat_label,
          topic.to_label as topic_label
        FROM
          JoinCategoryRole jcr,
          Role r,
          Category AS c
        LEFT JOIN Topic AS topic ON c.ca_id = Topic.to_category
        LEFT JOIN Category AS childCategory ON c.ca_id = childCategory.ca_parent
        WHERE
          r.ri_label = 'Public'
        AND
          jcr.jcr_role = r.ri_id
        AND
          jcr.jcr_category = c.ca_id
        AND
          c.ca_parent is null
      """
    val resultList: List[(String, Option[String], Option[String])] = SQL(sql).as(rootCategoryParser)
    if (Logger.isDebugEnabled) {
      Logger.debug("Topic.getPublic()")
      resultList.map(it => {
        Logger.debug(s"${it._1} ${it._2} ${it._3}")
      })
    }
    val categoryToProcess: Set[String] = resultList.map(_._1).toSet

    val rootCategories: Seq[RootCategory] = (categoryToProcess map {
      e: String =>
        val currentCategoryRows: List[(String, Option[String], Option[String])] = resultList
          .filter(res => res._1.equals(e))
        val subCategories = currentCategoryRows.filter(_._2.isDefined).map(_._2.get)
        val messages = currentCategoryRows.filter(_._3.isDefined).map(_._3.get)
        RootCategory(e, messages, subCategories)
    }).toSeq
    rootCategories

  }

  /**
    * Return root topic that are visible for the user given in parameter
    *
    * @param user       the current login user
    * @param connection data base connection
    * @return list of RootCategory
    */
  def getRoot(user: User)(implicit connection: Connection): Seq[RootCategory] = {
    val sql =
      """
            SELECT
              c.ca_label AS cat_label,
              childCategory.ca_label AS childCat_label,
              topic.to_label as topic_label
            FROM
              JoinUserRole jur,
              JoinCategoryRole jcr,
              Role r,
              Category AS c
            LEFT JOIN Topic AS topic ON c.ca_id = Topic.to_category
            LEFT JOIN Category AS childCategory ON c.ca_id = childCategory.ca_parent
            WHERE
              jur.jur_user = {userId}
            AND
              jur.jur_role = r.ri_id
            AND
              jcr.jcr_role = r.ri_id
            AND
              jcr.jcr_category = c.ca_id
            AND
              c.ca_parent is null
          """
    val resultList: List[(String, Option[String], Option[String])] = SQL(sql).on('userId -> user.id)
      .as(rootCategoryParser)
    if (Logger.isDebugEnabled) {
      Logger.debug(s"Topic.getRoot() ${user.login}")
      resultList.map(it => {
        Logger.debug(s"${it._1} ${it._2} ${it._3}")
      })
    }
    val categoryToProcess: Set[String] = resultList.map(_._1).toSet

    val rootCategories: Seq[RootCategory] = (categoryToProcess map {
      e: String =>
        val currentCategoryRows: List[(String, Option[String], Option[String])] = resultList
          .filter(res => res._1.equals(e))
        val subCategories = currentCategoryRows.filter(_._2.isDefined).map(_._2.get)
        val messages = currentCategoryRows.filter(_._3.isDefined).map(_._3.get)
        RootCategory(e, messages, subCategories)
    }).toSeq
    rootCategories
  }
}
