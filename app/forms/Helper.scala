package forms

import play.api.data.Form

import scala.collection.mutable.ListBuffer

/**
  * An helper that define commons function to get bootstrap classes related to forms.
  */
object Helper {

  /**
    * Decorate the div with class form-group element with has-danger, has-success or nothing.
    * @param form the form
    * @param field the field define in this div element
    * @tparam A Form parameter type
    * @return
    */
  def decorateFormGroup[A <: AnyRef](form: Form[A], field: String): Option[String] = {
    if (form.error(field).isDefined)
      Some("has-danger")
    else if (form.data.get(field).isDefined && !form.data.get(field).equals(""))
      Some("has-success")
    else None
  }

  def decorateField[A <: AnyRef](form: Form[A], field: String): Option[String] = {
    if (inError(form, field)) {
      Some("form-control-danger")
    } else if (filled(form, field)) {
      Some("form-control-success")
    } else {
      None
    }
  }

  private def inError[A <: AnyRef](form: Form[A], field: String): Boolean = {
    !form.error(field).isEmpty
  }

  private def filled[A <: AnyRef](form: Form[A], field: String): Boolean = {
    !form(field).value.isEmpty
  }
}
