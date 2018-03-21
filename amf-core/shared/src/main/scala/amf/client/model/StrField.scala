package amf.client.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait StrField extends BaseField with ValueField {

  override type ValueType = String

  /** Return value or null if value is null or undefined. */
  override def value(): String = option().orNull

  /** Return true if string value is null or empty. */
  def isNullOrEmpty: Boolean = option().fold(true)(_.isEmpty)

  /** Return true if string value is not null and not empty. */
  def nonEmpty: Boolean = !isNullOrEmpty
}
