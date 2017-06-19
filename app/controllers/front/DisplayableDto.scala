package controllers.front

import services.IdEncryptionUtil

/**
  * Parent of all bean that will be send to client and have got a primary key
  * @author tweissbeck
  */
abstract class DisplayableDto(id: Long) {

  def getEncoded(): String={
    IdEncryptionUtil.encode(id)
  }
}
