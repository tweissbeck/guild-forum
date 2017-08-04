package services.intern

import java.sql.Connection
import java.time.LocalDateTime

import anorm.SqlParser._
import anorm._
import services.intern.database._

/**
  * @author tweissbeck
  */
object ApplicationService {


  /**
    * Simple application with ID, Status, Data, CreationDate
    */
  type APPLICATION_DB_TYPE = (Long, String, String, LocalDateTime)
  private val APPLICATION_PARSER: RowParser[APPLICATION_DB_TYPE] =
    get[Long](Application.ID) ~ get[String](Application.STATUS) ~
      get[String](Application.DATA) ~ get[LocalDateTime](Application.CREATION_DATE) map {
      case id ~ status ~ data ~ create => (id, status, data, create)
    }

  type APPLICATION_DB_TYPE_FULL = (Long, String, String, LocalDateTime, Long, String, String, String)
  private val APPLICATION_PARSER_WITH_USER: RowParser[APPLICATION_DB_TYPE_FULL] =
    get[Long](Application.ID) ~ get[String](Application.STATUS) ~
      get[String](Application.DATA) ~ get[LocalDateTime](Application.CREATION_DATE) ~ get[Long](User.ID) ~
      get[String](User.FIRST_NAME) ~ get[String](User.LAST_NAME) ~ get[String](User.MAIL) map {
      case id ~ status ~ data ~ create ~ userId ~ userFisrtName ~ userLastName ~
        userMail => (id, status, data, create, userId, userFisrtName, userLastName, userMail)
    }
  private val APPLICATIONS_PARSER: ResultSetParser[List[APPLICATION_DB_TYPE]] = (APPLICATION_PARSER.*)
  private val APPLICATIONS_WITH_USER_PARSER: ResultSetParser[List[APPLICATION_DB_TYPE_FULL]] = (APPLICATION_PARSER_WITH_USER
    .*)

  /**
    * Return application with status equals to 'NEW'
    *
    * @param connection sql connection
    * @return
    */
  def getNew()(implicit connection: Connection): Seq[ApplicationWithUser] = {
    val query = SQL(
      s"SELECT * FROM ${Application.TABLE_NAME}, ${User.TABLE_NAME} WHERE ${Application.STATUS} = 'NEW' AND ${
        User.ID
      } = ${Application.USER}")
    val result: List[APPLICATION_DB_TYPE_FULL] = query.as(APPLICATIONS_WITH_USER_PARSER)
    result
      .map(app => ApplicationWithUser(Some(app._1), app._2, app._4, app._3,
        ApplicationUser(app._5, app._6, app._7, app._8)))
  }

  /**
    * Insert a new application with user that create it
    *
    * @param application application to insert
    * @param id          primary of uer who created this application
    * @return
    */
  def insert(application: ApplicationDetail, id: Long)(implicit connection: Connection): Long = {
    val sqlQeury = SQL(
      s"""INSERT INTO ${Application.TABLE_NAME}
         (${Application.STATUS}, ${Application.CREATION_DATE}, ${Application.DATA}, ${Application.USER}) VALUES (
            '${application.ap_status}', {createdAt}, {data}, {user}
         );
        """
    )
    val applicationId: Option[Long] = sqlQeury.on(
      "createdAt" -> application.ap_creationDate,
      "data" -> application.data,
      "user" -> id
    ).executeInsert()
    applicationId.get
  }

  def findById(id: Long)(implicit connection: Connection): Option[ApplicationWithUser] = {
    val query = SQL(
      s"SELECT * FROM ${Application.TABLE_NAME}, ${User.TABLE_NAME} WHERE ${Application.STATUS} = 'NEW' AND ${
        User.ID
      } = ${Application.USER}")
    val resultOption: Option[APPLICATION_DB_TYPE_FULL] = query.as(APPLICATION_PARSER_WITH_USER.singleOpt)
    if (resultOption.isDefined) {
      val result = resultOption.get
      Some(ApplicationWithUser(Some(result._1), result._2, result._4, result._3,
        ApplicationUser(result._5, result._6, result._7, result._8)))
    } else {
      None
    }

  }
}
