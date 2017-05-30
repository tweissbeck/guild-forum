package services.intern.database

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import controllers.forum.{Category, DisplayableElement}
import play.api.Logger

/**
 * This object give access to Topic data.
 *
 * @author tweissbeck
 */
object Topic {

  type CategoryParser = (Long, String, Option[Long], Option[String], Option[Long],
    Option[String])

  private val rootCategoryParser: ResultSetParser[List[CategoryParser]] = {
    (long("cat_id") ~ str("cat_label") ~ get[Option[Long]]("chieldCat_id") ~ get[Option[String]]("childCat_label") ~
      get[Option[Long]]("topic_id") ~ get[Option[String]]("topic_label") map
      (flatten)) *
  }

  private def transformToCategory(rows: List[CategoryParser], tuple: (Long, String)): Category = {
    val currentCategoryRows: List[CategoryParser] = rows
      .filter(res => res._1.equals(tuple._1))
    val subCategories: Seq[(Long, String)] = currentCategoryRows.filter(_._3.isDefined).map(subCategory =>
      (subCategory._3.get, subCategory._4.get))
    val messages: Seq[(Long, String)] = currentCategoryRows.filter(_._5.isDefined).map(message => (message._5
      .get, message._6.get))
    new Category(tuple._1, tuple._2, messages.map(message => new DisplayableElement(message._2, message._1) {}),
      subCategories.map(subCategory => new DisplayableElement(subCategory._2, subCategory._1) {}))
  }

  def getPublic()(implicit connection: Connection): Seq[Category] = {
    val sql =
      """
        SELECT
          c.ca_id AS cat_id,
          c.ca_label AS cat_label,
          childCategory.ca_label AS childCat_label,
          childCategory.ca_id AS chieldCat_id,
          topic.to_label as topic_label,
          topic.to_id AS topic_id
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
    val resultList: List[CategoryParser] = SQL(sql).as(rootCategoryParser)
    if (Logger.isDebugEnabled) {
      Logger.debug("Topic.getPublic()")
      resultList.map(it => {
        Logger.debug(s"${it._1} ${it._2} ${it._3}")
      })
    }
    val categoryToProcess: Set[(Long, String)] = resultList.map(result => (result._1 -> result._2)).toSet

    val rootCategories: Seq[Category] = (categoryToProcess map {
      tuple: (Long, String) =>
        val currentCategoryRows: List[CategoryParser] = resultList
          .filter(res => res._1.equals(tuple._1))
        transformToCategory(currentCategoryRows, tuple)
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
  def getRoot(user: User)(implicit connection: Connection): Seq[Category] = {
    val sql =
      """
            SELECT
              c.ca_id AS cat_id,
              c.ca_label AS cat_label,
              childCategory.ca_label AS childCat_label,
              childCategory.ca_id AS chieldCat_id,
              topic.to_label as topic_label,
              topic.to_id AS topic_id
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
    val resultList: List[CategoryParser] = SQL(sql).on('userId -> user.id)
      .as(rootCategoryParser)
    if (Logger.isDebugEnabled) {
      Logger.debug(s"Topic.getRoot() ${user.login}")
      resultList.map(it => {
        Logger.debug(s"${it._1} ${it._2} ${it._3}")
      })
    }
    val categoryToProcess: Set[(Long, String)] = resultList.map(rows => (rows._1, rows._2)).toSet

    val rootCategories: Seq[Category] = (categoryToProcess map {
      tuple: (Long, String) =>
        val currentCategoryRows: List[CategoryParser] = resultList
          .filter(res => res._1.equals(tuple._1))
        transformToCategory(currentCategoryRows, tuple)
    }).toSeq
    rootCategories
  }

  def getCategory(categoryId: Long, user: Option[User])(implicit connection: Connection): Option[Category] = {

    val query = if (user.isDefined)
      SQL(
        """
        SELECT
           c.ca_id AS cat_id,
           c.ca_label AS cat_label,
           childCategory.ca_label AS childCat_label,
           childCategory.ca_id AS chieldCat_id,
           topic.to_label as topic_label,
           topic.to_id AS topic_id
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
           c.ca_id = {id}
      """).on('id -> categoryId, 'userId -> user.get.id)
    else
      SQL(
        """
        SELECT
          c.ca_id AS cat_id,
          c.ca_label AS cat_label,
          childCategory.ca_label AS childCat_label,
          childCategory.ca_id AS chieldCat_id,
          topic.to_label as topic_label,
          topic.to_id AS topic_id
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
          c.ca_id = {id}
      """).on('id -> categoryId)
    val result: List[CategoryParser] = query.as(rootCategoryParser)
    // The logged in user should not right on this topic or the primary key do not exist.
    if (result.isEmpty) {
      None
    } else {
      Some(transformToCategory(result, (categoryId, result.head._2)));
    }
  }
}
