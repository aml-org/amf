package amf.client.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait AnyField extends BaseField with ValueField {

  override type ValueType = Any

  /** Return value or null if value is null or undefined. */
  override def value(): Any = option().orNull
}
