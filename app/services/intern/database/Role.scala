package services.intern.database

import java.sql.Connection

import anorm.{Macro, SQL}

case class Role(id: Long, label: String)

/**
  * @author tweissbeck
  */
object Role {

  def getAll()(implicit connection: Connection): Seq[Role] = {
    val query = SQL("SELECT * FROM Role")
    query.as(Macro.namedParser[Role].*)
  }

}
