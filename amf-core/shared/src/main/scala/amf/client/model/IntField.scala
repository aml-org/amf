package amf.client.model

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait IntField extends BaseAnyValField {

  override type ValueType = Int

  /** Return int value or `0` if value is null or undefined. */
  override def value(): Int = option() match {
    case Some(v) => v
    case _       => 0
  }
}
