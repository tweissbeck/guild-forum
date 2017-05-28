package controllers.forum

import services.IdEncryptionUtil


/**
 * @param id the id of diplayed entity. This id is private because we don't want to expose such data base data. Use [[encoded()]] to link this entity in views.
 */
abstract class DisplayableElement(val label: String, private val id: Long) {

  /**
   * @return the encoded id of this entity.
   */
  def encoded(): String = {
    IdEncryptionUtil.encode(this.id)
  }

}
