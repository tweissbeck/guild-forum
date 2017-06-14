package services.intern.database

import java.sql.Connection

import anorm.SqlParser._
import anorm._


case class Role(id: Long, label: String)

/**
  * @author tweissbeck
  */
object Role {

  private val roleParser: ResultSetParser[List[(Long, String)]] = {
    (long("ri_id") ~ str("ri_label") map
      (flatten)) *
  }

  private implicit def tranform: ((Long, String)) => Role = {
    result: (Long, String) => new Role(result._1, result._2)
  }

  def getAll()(implicit connection: Connection): Seq[Role] = {
    val query = SQL("SELECT * FROM Role")
    query.as(roleParser).map(e => tranform(e))
  }

}
