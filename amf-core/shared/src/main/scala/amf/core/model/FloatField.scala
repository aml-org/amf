package amf.core.model

trait FloatField extends BaseAnyValField[Float] {

  /** Return float value or `0.0f` if value is null or undefined. */
  override def value(): Float = option() match {
    case Some(v) => v
    case _       => 0.0f
  }
}
