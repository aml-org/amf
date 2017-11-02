package amf.model

/**
  * JVM VariableValue model class.
  */
case class VariableValue private[model] (private val variable: amf.domain.`abstract`.VariableValue)
    extends DomainElement {
  def this() = this(amf.domain.`abstract`.VariableValue())

  val name: String  = variable.name
  val value: String = variable.value

  def withName(name: String): this.type = {
    variable.withName(name)
    this
  }

  def withValue(value: String): this.type = {
    variable.withValue(value)
    this
  }

  override private[amf] def element: amf.domain.`abstract`.VariableValue = variable
}
