package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.ParameterModel._

/**
  * Parameter internal model.
  */
case class Parameter(fields: Fields, annotations: Annotations) extends DomainElement {

  val name: String        = fields(Name)
  val description: String = fields(Description)
  val required: Boolean   = fields(Required)
  val binding: String     = fields(Binding)
  val schema: String      = fields(Schema)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withRequired(required: Boolean): this.type      = set(Required, required)
  def withBinding(binding: String): this.type         = set(Binding, binding)
  def withSchema(schema: String): this.type           = set(Schema, schema)

  def isHeaderType: Boolean = binding == "header"
  def isQueryType: Boolean  = binding == "query"
}

object Parameter {
  def apply(fields: Fields = Fields(), annotations: Annotations = new Annotations()): Parameter =
    new Parameter(fields, annotations)

  def apply(ast: AMFAST): Parameter = new Parameter(Fields(), Annotations(ast))
}
