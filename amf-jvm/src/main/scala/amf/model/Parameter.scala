package amf.model

/**
  * Parameter jvm class
  */
case class Parameter private[model] (private val parameter: amf.domain.Parameter) extends DomainElement {

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
}
