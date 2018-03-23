package amf.client.model

trait FloatField extends BaseAnyValField {

  override type ValueType = Float

  /** Return float value or `0.0f` if value is null or undefined. */
  override def value(): Float = option() match {
    case Some(v) => v
    case _       => 0.0f
  }
}
