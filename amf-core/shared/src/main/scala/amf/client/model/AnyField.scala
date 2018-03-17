package amf.client.model

trait AnyField extends BaseField with ValueField{

  override type ValueType = Any

  /** Return int value. */
  override def value(): Any
}
