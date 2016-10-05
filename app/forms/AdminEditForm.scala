package forms

/**
  *
  */
case class AdminEditForm(id: Long, lastName: String, firstName: String, login: Option[String], email: String,
                         password: Option[String], isAdmin: Boolean) {

}
