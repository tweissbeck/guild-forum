package controllers.front

/**
  *
  * @author tweissbeck
  */
case class Category(id: Long, label: String) extends DisplayableDto(id) {
  def this(category: services.intern.database.forum.Category) = {
    this(category.ca_id, category.ca_label)
  }
}

