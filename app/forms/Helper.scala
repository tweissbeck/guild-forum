package forms

import play.api.data.Form
import play.api.i18n.MessagesApi

import scala.xml.Elem

/**
  * An helper that define commons function to apply forms bootstrap classes.
  */
object Helper {

  /**
    * Decorate the div with class form-group element with has-danger, has-success or nothing.
    *
    * @param form  the form
    * @param field the field define in this div element
    * @tparam A Form parameter type
    * @return
    */
  def decorateFormGroup[A <: AnyRef](form: Form[A], field: String, onlyError: Boolean = false): Option[String] = {
    if (form.error(field).isDefined)
      Some("has-danger")
    else if (form.data.get(field).isDefined && !form.data.get(field).get.isEmpty && !onlyError)
      Some("has-success")
    else None
  }

  /**
    * Decorate the input tag with class related to validation.
    * Add form-control-danger, form-control-success or nothing depending on the error, value of the field in the form
    *
    * @param form      the form
    * @param field     the field key in the form
    * @param onlyError add only form-control-danger if the field get errors
    * @tparam A the type of the form
    * @return
    */
  def decorateField[A <: AnyRef](form: Form[A], field: String, onlyError: Boolean = false): Option[String] = {
    if (inError(form, field)) {
      Some("form-control-danger")
    } else if (filled(form, field) && !onlyError) {
      Some("form-control-success")
    } else {
      None
    }
  }

  def checked[A <: AnyRef](form: Form[A], field: String): Boolean = {
    filled(form, field) && form.data.get(field).get.equals("true")
  }

  private def inError[A <: AnyRef](form: Form[A], field: String): Boolean = form.error(field).isDefined

  /**
    * Add value="${form.value}" if data is filled in the form.
    */
  def fieldValue[A <: AnyRef](form: Form[A], field: String): Option[String] = {
    if (filled(form, field)) {
      val value =
        s"""
          value=${form.data.get(field).get}
        """
      Some(value)
    }
    else
      None
  }

  def value[A <: AnyRef](form: Form[A], field: String): Option[String] = {
    if (filled(form, field)) {
      val value = form.data.get(field).get
      Some(value)
    }
    else
      None
  }


  private def filled[A <: AnyRef](form: Form[A], field: String): Boolean = form.data.get(field).isDefined &&
    !form.data.get(field)
      .get.isEmpty

  def fieldFeedback[A <: AnyRef](form: Form[A], field: String)(implicit messagesApi: MessagesApi): Option[Elem] = {
    if (inError(form, field)) {
      val html = <div class="form-control-feedback">
        {messagesApi.apply(form.error(field).get.messages.head)}
      </div>
      Some(html)
    } else
      None
  }
}
