package amf.client.model

trait AnyField extends BaseField with ValueField {

  override type ValueType = Any

  /** Return value or null if value is null or undefined. */
  override def value(): Any = option().orNull
}
