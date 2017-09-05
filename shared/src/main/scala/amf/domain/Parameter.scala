package amf.domain

import amf.common.AMFAST
import amf.metadata.domain.ParameterModel._

/**
  * Parameter internal model.
  */
case class Parameter(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: String        = fields(Name)
  def description: String = fields(Description)
  def required: Boolean   = fields(Required)
  def binding: String     = fields(Binding)
  def schema: String      = fields(Schema)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withRequired(required: Boolean): this.type      = set(Required, required)
  def withBinding(binding: String): this.type         = set(Binding, binding)
  def withSchema(schema: String): this.type           = set(Schema, schema)

  def isHeader: Boolean = binding == "header"
  def isQuery: Boolean  = binding == "query"
  def isBody: Boolean   = binding == "body"
  def isPath: Boolean   = binding == "path"

  override def adopted(parent: String): this.type = withId(parent + "/parameter/" + name)

}

object Parameter {
  def apply(): Parameter = apply(Annotations())

  def apply(ast: AMFAST): Parameter = apply(Annotations(ast))

  def apply(annotations: Annotations): Parameter = new Parameter(Fields(), annotations)
}
