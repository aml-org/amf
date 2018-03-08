package amf.client.model

trait BoolField extends BaseField with ValueField {

  override type ValueType = Boolean

  /** Return boolean value. */
  override def value(): Boolean
}