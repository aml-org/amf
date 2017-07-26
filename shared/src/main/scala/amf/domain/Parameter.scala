package amf.domain

import amf.builder.ParameterBuilder
import amf.metadata.domain.ParameterModel._

/**
  * Parameter internal model.
  */
case class Parameter(fields: Fields) extends DomainElement {
  override type T = Parameter

  val name: String        = fields(Name)
  val description: String = fields(Description)
  val required: Boolean   = fields(Required)
  val binding: String     = fields(Binding)
  val schema: String      = fields(Schema)

  def canEqual(other: Any): Boolean = other.isInstanceOf[Parameter]

  override def equals(other: Any): Boolean = other match {
    case that: Parameter =>
      (that canEqual this) &&
        name == that.name &&
        description == that.description &&
        required == that.required &&
        binding == that.binding &&
        schema == that.schema

    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, description, required, binding, schema)
    state.map(p => if (p != null) p.hashCode() else 0).foldLeft(0)((a, b) => 31 * a + b)
  }

  override def toString = s"Parameter($name, $description, $required, $binding, $schema)"

  override def toBuilder: ParameterBuilder = ParameterBuilder(fields)
}

object Parameter {
  def apply(fields: Fields): Parameter = new Parameter(fields)
}
