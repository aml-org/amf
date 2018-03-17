package amf.client.model

trait ValueField {

  type ValueType

  /** Return string value. */
  def value(): ValueType

  /** Return string value as option. */
  def option(): Option[ValueType] = if(present()) Option(value()) else None


  /** Return true if string value is equals to non-null given value. */
  def is(other: ValueType): Boolean = option().fold(false)(_ == other)

  def isNull: Boolean = option().isEmpty

  def present(): Boolean

  override def toString: String = option().map(_.toString).orNull // null
}
