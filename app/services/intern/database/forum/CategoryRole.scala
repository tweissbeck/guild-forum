package services.intern.database.forum

/**
  * Association between category and role
  *
  * @param ca_id    category primary key
  * @param ca_label category label
  * @param ri_id    role primary key
  * @param ri_label role label
  */
case class CategoryRole(ca_id: Long, ca_label: String, ri_id: Long, ri_label: String) {

}

/**
  * Association between category and role
  *
  * @param ca_id    category primary key
  * @param ca_label category label
  * @param ri_id    role primary key
  * @param ri_label role label
  * @param create   can the role view topic under its category
  * @param view     can the role create new topic under its category
  */
case class CategoryRoleWithRights(ca_id: Long, ca_label: String, ri_id: Long, ri_label: String, view: Boolean,
                                  create: Boolean) {

}
