package amf.core.model

trait ValueField[T] {

  /** Return string value as option. */
  def option(): Option[T]

  /** Return value or null. */
  def value(): T

  /** Return true if string value is equals to non-null given value. */
  def is(other: T): Boolean = option().fold(false)(_ == other)

  /** Return true if string value is not-null and accepted by given function. */
  def is(accepts: T => Boolean): Boolean = option().fold(false)(accepts(_))

  /** Returns true if field is null. */
  def isNull: Boolean = option().isEmpty

  /** Returns true if field is non null. */
  def nonNull: Boolean = option().isDefined

  override def toString: String = option().map(_.toString).orNull // null
}
