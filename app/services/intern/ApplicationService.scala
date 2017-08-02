package services.intern

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._
import services.intern.database.Application

/**
  * @author tweissbeck
  */
object ApplicationService {


  type APPLICATION_DB_TYPE = (Long, String, String, LocalDateTime)
  private val APPLICATION_PARSER: RowParser[APPLICATION_DB_TYPE] =
    get[Long](Application.ID) ~ get[String](Application.STATUS) ~
      get[String](Application.DATA) ~ get[LocalDateTime](Application.CREATION_DATE) map {
      case id ~ status ~ data ~ create => (id, status, data, create)
    }
  private val APPLICATIONS_PARSER: ResultSetParser[List[APPLICATION_DB_TYPE]] = (APPLICATION_PARSER.*)

  /**
    * Return application with status equals to 'NEW'
    *
    * @param connection sql connection
    * @return
    */
  def getNew()(implicit connection: Connection): Seq[Application] = {
    val query = SQL(s"SELECT * FROM ${Application.TABLE_NAME} WHERE ${Application.STATUS} = 'NEW'")
    val result: List[APPLICATION_DB_TYPE] = query.as(APPLICATIONS_PARSER)
    result.map(app => Application(app._1, app._2, app._3, app._4))
  }
}
