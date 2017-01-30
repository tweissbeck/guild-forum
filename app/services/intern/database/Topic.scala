package services.intern.database

import java.sql.Connection
import anorm.SqlParser._
import anorm._

/**
  * Created by tweissbeck on 14/10/2016.
  */
object Topic {

  val topicParser = get[Long]("id") ~ get[String]("label")
  def getPublic()(implicit connection: Connection): Unit = {
    val sql =
      """
        SELECT
          c.*
        FROM
          Category c,
          JoinCategoryRight jcr,
          Role r
        WHERE
          r.ri_label = 'Public'
        AND
          jcr.jcr_right = r.ri_id
        AND
          jcr.jcr_category = c.ca_id
        AND
          c.ca_parent is null
      """
    //SQL(sql).as()

  }

  def getRoot(user: User)(implicit connection: Connection): Unit = {

  }
}
