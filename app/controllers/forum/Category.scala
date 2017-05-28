package controllers.forum

/**
 * @author tweissbeck
 */
class Category(private val id: Long, override val label: String, val messages: Seq[DisplayableElement], val childs: Seq[DisplayableElement]) extends DisplayableElement(label, id) {

}

