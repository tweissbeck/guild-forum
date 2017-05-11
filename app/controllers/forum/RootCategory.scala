package controllers.forum

case class RootCategory(categoryName: String, messages: Seq[String], childs: Seq[String])
