package services.intern.database.forum

/**
  * Association between category and role
  *
  * @param catId         category primary key
  * @param categoryLabel category label
  * @param roleId        role primary key
  * @param roleLabel     role label
  */
case class CategoryRole(catId: Long, categoryLabel: String, roleId: Long, roleLabel: String) {

}

/**
  * Association between category and role
  *
  * @param catId         category primary key
  * @param categoryLabel category label
  * @param roleId        role primary key
  * @param roleLabel     role label
  * @param create        can the role view topic under its category
  * @param view          can the role create new topic under its category
  */
case class CategoryRoleWithRights(catId: Long, categoryLabel: String, roleId: Long, roleLabel: String, view: Boolean,
                                  create: Boolean) {

}
