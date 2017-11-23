package amf.domain

import amf.framework.parser.Annotations
import amf.metadata.domain.ParameterModel
import amf.metadata.domain.ParameterModel._
import amf.shape.{NodeShape, ScalarShape, Shape}
import org.yaml.model.YPart

/**
  * Parameter internal model.
  */
case class Parameter(fields: Fields, annotations: Annotations) extends DomainElement with Linkable {

  def name: String        = fields(Name)
  def description: String = fields(Description)
  def required: Boolean   = fields(Required)
  def binding: String     = fields(Binding)
  def schema: Shape       = fields(Schema)

  def withName(name: String): this.type               = set(Name, name)
  def withDescription(description: String): this.type = set(Description, description)
  def withRequired(required: Boolean): this.type      = set(Required, required)
  def withBinding(binding: String): this.type         = set(Binding, binding)
  def withSchema(schema: Shape): this.type            = set(Schema, schema)

  def isHeader: Boolean = binding == "header"
  def isQuery: Boolean  = binding == "query"
  def isBody: Boolean   = binding == "body"
  def isPath: Boolean   = binding == "path"

  override def adopted(parent: String): this.type = withId(parent + "/parameter/" + name)

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(ParameterModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(ParameterModel.Schema, scalar)
    scalar
  }

  override def linkCopy(): Parameter = Parameter().withBinding(binding).withId(id)

  def cloneParameter(parent: String): Parameter = {
    val cloned = Parameter(annotations).withName(name).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape()
          case o        => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }
}

object Parameter {
  def apply(): Parameter = apply(Annotations())

  def apply(ast: YPart): Parameter = apply(Annotations(ast))

  def apply(annotations: Annotations): Parameter = new Parameter(Fields(), annotations)
}
