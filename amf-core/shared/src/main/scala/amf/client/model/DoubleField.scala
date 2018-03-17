package amf.client.model

trait DoubleField extends BaseField with ValueField {

  override type ValueType = Double

  /** Return int value. */
  override def value(): Double
}
