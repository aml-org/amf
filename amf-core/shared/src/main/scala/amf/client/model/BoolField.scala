package amf.client.model

trait BoolField extends BaseAnyValField {

  override type ValueType = Boolean

  /** Return boolean value or `false` if value is null or undefined. */
  override def value(): Boolean = option() match {
    case Some(v) => v
    case _       => false
  }
}
