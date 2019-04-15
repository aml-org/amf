package amf.plugins.domain.webapi.models

import amf.core.metamodel.{Field, Obj}
import amf.core.model.{BoolField, StrField}
import amf.core.model.domain.{DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.shapes.models.{Example, Examples, NodeShape, ScalarShape}
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.metamodel.ParameterModel.{Examples => ExamplesField, _}
import org.yaml.model.YPart
import amf.core.utils.Strings

/**
  * Parameter internal model.
  */
class Parameter(override val fields: Fields, override val annotations: Annotations)
    extends DomainElement
    with Linkable
    with NamedDomainElement {

  def parameterName: StrField    = fields.field(ParameterName)
  def description: StrField      = fields.field(Description)
  def required: BoolField        = fields.field(Required)
  def deprecated: BoolField      = fields.field(Deprecated)
  def allowEmptyValue: BoolField = fields.field(AllowEmptyValue)
  def style: StrField            = fields.field(Style)
  def explode: BoolField         = fields.field(Explode)
  def allowReserved: BoolField   = fields.field(AllowReserved)
  def binding: StrField          = fields.field(Binding)
  def schema: Shape              = fields.field(Schema)
  def payloads: Seq[Payload]     = fields.field(Payloads)
  def examples: Examples         = fields.field(ExamplesField)

  def withParameterName(name: String): this.type               = set(ParameterName, name)
  def withDescription(description: String): this.type          = set(Description, description)
  def withRequired(required: Boolean): this.type               = set(Required, required)
  def withDeprecated(deprecated: Boolean): this.type           = set(Deprecated, deprecated)
  def withAllowEmptyValue(allowEmptyValue: Boolean): this.type = set(AllowEmptyValue, allowEmptyValue)
  def withStyle(style: String): this.type                      = set(Style, style)
  def withExplode(explode: Boolean): this.type                 = set(Explode, explode)
  def withAllowReserved(allowReserved: Boolean): this.type     = set(AllowReserved, allowReserved)
  def withBinding(binding: String): this.type                  = set(Binding, binding)
  def withSchema(schema: Shape): this.type                     = set(Schema, schema)
  def withPayloads(payloads: Seq[Payload]): this.type          = setArray(Payloads, payloads)
  def withExamples(examples: Examples): this.type              = set(ExamplesField, examples)
  def withExamples(examples: Seq[Example]): this.type = {
    val ex = Examples()
    set(ExamplesField, ex)
    ex.withExamples(examples)
    this
  }

  def isHeader: Boolean = binding.is("header")
  def isQuery: Boolean  = binding.is("query")
  def isBody: Boolean   = binding.is("body")
  def isPath: Boolean   = binding.is("path")
  def isForm: Boolean   = binding.is("formData")
  def isCookie: Boolean = binding.is("cookie")

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

  def withPayload(mediaType: String): Payload = {
    val result = Payload().withMediaType(mediaType)
    add(ParameterModel.Payloads, result)
    result
  }

  def withExample(name: Option[String]): Example = {
    val newExample = Example()
    name.foreach(newExample.withName(_))
    examples match {
      case e: Examples => e ++ Seq(newExample)
      case _ =>
        val newExamples = Examples()
        withExamples(newExamples)
        newExamples.withExamples(Seq(newExample))
    }
    newExample
  }

  def exampleValues: Seq[Example] = examples match {
    case e: Examples => e.examples
    case _           => Nil
  }

  override def linkCopy(): Parameter = Parameter().withBinding(binding.value()).withId(id)

  def cloneParameter(parent: String): Parameter = {
    val cloned = Parameter(Annotations(annotations)).withName(name.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape(None)
          case o        => o
        }

        cloned.set(f, clonedValue, v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: Obj = ParameterModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  override def componentId: String = "/parameter/" + name.option().getOrElse("default-parameter").urlComponentEncoded

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Parameter.apply
  override protected def nameField: Field                                                       = Name
}

object Parameter {
  def apply(): Parameter = apply(Annotations())

  def apply(ast: YPart): Parameter = apply(Annotations(ast))

  def apply(annotations: Annotations): Parameter = new Parameter(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Parameter = new Parameter(fields, annotations)
}
