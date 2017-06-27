package controllers.front

/**
  * @author tweissbeck
  */
case class Role(id: Long, label: String) extends DisplayableDto(id) {
  def this(role: services.intern.database.Role) = {
    this(role.id, role.label)
  }
}
