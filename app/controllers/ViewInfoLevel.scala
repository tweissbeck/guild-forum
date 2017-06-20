package controllers


sealed trait InfoLevel {

}

object WARN extends InfoLevel

object ERROR extends InfoLevel

object INFO extends InfoLevel
