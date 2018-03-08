package amf.client.model

trait IntField extends BaseField with ValueField {

  override type ValueType = Int

  /** Return int value. */
  override def value(): Int
}