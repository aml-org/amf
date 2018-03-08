package amf.client.model

trait ValueField {

  type ValueType

  /** Return string value. */
  def value(): ValueType

  /** Return string value as option. */
  def option(): Option[ValueType] = Option(value())

  /** Return true if string value is equals to non-null given value. */
  def is(other: ValueType): Boolean = option().fold(false)(_ == other)

  override def toString: String = /*option().orNull*/ null
}
