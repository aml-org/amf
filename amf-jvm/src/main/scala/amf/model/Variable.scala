package amf.model

/**
  * JVM Variable model class.
  */
case class Variable private[model] (private val variable: amf.domain.`abstract`.Variable) extends DomainElement {
  def this() = this(amf.domain.`abstract`.Variable())

  val name: String           = variable.name
  val transformation: String = variable.transformation

  def withName(name: String): this.type = {
    variable.withName(name)
    this
  }

  def withTransformation(transformation: String): this.type = {
    variable.withTransformation(transformation)
    this
  }

  override private[amf] def element: amf.domain.`abstract`.Variable = variable
}

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
