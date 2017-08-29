package amf.model

/**
  * JVM Parameter model class.
  */
case class Parameter private[model] (private val parameter: amf.domain.Parameter) extends DomainElement {

  def this() = this(amf.domain.Parameter())

  val name: String        = parameter.name
  val description: String = parameter.description
  val required: Boolean   = parameter.required
  val binding: String     = parameter.binding
  val schema: String      = parameter.schema

  override def equals(other: Any): Boolean = other match {
    case that: Parameter =>
      (that canEqual this) &&
        parameter == that.parameter
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[Parameter]

  override private[amf] def element: amf.domain.Parameter = parameter

  /** Set name property of this [[Parameter]]. */
  def withName(name: String): this.type = {
    parameter.withName(name)
    this
  }

  /** Set description property of this [[Parameter]]. */
  def withDescription(description: String): this.type = {
    parameter.withDescription(description)
    this
  }

  /** Set required property of this [[Parameter]]. */
  def withRequired(required: Boolean): this.type = {
    parameter.withRequired(required)
    this
  }

  /** Set binding property of this [[Parameter]]. */
  def withBinding(binding: String): this.type = {
    parameter.withBinding(binding)
    this
  }

  /** Set schema property of this [[Parameter]]. */
  def withSchema(schema: String): this.type = {
    parameter.withSchema(schema)
    this
  }
}
