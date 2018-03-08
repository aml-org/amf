package amf.client.model

trait StrField extends BaseField with ValueField {

  override type ValueType = String

  /** Return string value. */
  override def value(): String

  /** Return true if string value is null or empty. */
  def isNullOrEmpty: Boolean = option().fold(true)(_.isEmpty)
}
