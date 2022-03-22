package amf.apicontract.client.scala.model.domain

import amf.apicontract.internal.metamodel.domain.ParameterModel
import amf.apicontract.internal.metamodel.domain.ParameterModel._
import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.ScalarShape
import amf.shapes.client.scala.model.domain.operations.AbstractParameter
import amf.shapes.client.scala.model.domain.{ExemplifiedDomainElement, NodeShape, ScalarShape}
import org.yaml.model.YPart

/**
  * Parameter internal model.
  */
class Parameter(override val fields: Fields, override val annotations: Annotations)
    extends AbstractParameter(fields, annotations)
    with Linkable
    with SchemaContainer
    with ExemplifiedDomainElement {

  def deprecated: BoolField      = fields.field(Deprecated)
  def allowEmptyValue: BoolField = fields.field(AllowEmptyValue)
  def style: StrField            = fields.field(Style)
  def explode: BoolField         = fields.field(Explode)
  def allowReserved: BoolField   = fields.field(AllowReserved)
  def payloads: Seq[Payload]     = fields.field(Payloads)

  def withDeprecated(deprecated: Boolean): this.type           = set(Deprecated, deprecated)
  def withAllowEmptyValue(allowEmptyValue: Boolean): this.type = set(AllowEmptyValue, allowEmptyValue)
  def withStyle(style: String): this.type                      = set(Style, style)
  def withExplode(explode: Boolean): this.type                 = set(Explode, explode)
  def withAllowReserved(allowReserved: Boolean): this.type     = set(AllowReserved, allowReserved)
  def syntheticBinding(binding: String): this.type =
    set(Binding, AmfScalar(binding), Annotations.synthesized())
  def withPayloads(payloads: Seq[Payload]): this.type = setArray(Payloads, payloads)

  override protected def buildParameter(ann: Annotations): Parameter = Parameter(ann)

  override def setSchema(shape: Shape): Shape = {
    set(ParameterModel.Schema, shape)
    shape
  }

  def isHeader: Boolean = binding.is("header")
  def isQuery: Boolean  = binding.is("query")
  def isBody: Boolean   = binding.is("body")
  def isPath: Boolean   = binding.is("path")
  def isForm: Boolean   = binding.is("formData")
  def isCookie: Boolean = binding.is("cookie")

  def withPayload(mediaType: String): Payload = {
    val result = Payload().withMediaType(mediaType)
    add(ParameterModel.Payloads, result)
    result
  }

  override def linkCopy(): Parameter = {
    val copy = Parameter().withId(id)
    binding.option().foreach(copy.set(ParameterModel.Binding, _, Annotations.synthesized()))
    copy
  }

  override def cloneParameter(parent: String): Parameter = {
    val parameter: Parameter = Parameter(Annotations(annotations))
    val cloned               = parameter.withName(name.value()).adopted(parent)

    this.fields.foreach {
      case (f, v) =>
        val clonedValue = v.value match {
          case s: Shape => s.cloneShape(None)
          case o        => o
        }

        cloned.set(f, clonedValue, Annotations() ++= v.annotations)
    }

    cloned.asInstanceOf[this.type]
  }

  override def meta: DomainElementModel = ParameterModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    s"/parameter/${encoded(binding, "default-binding")}/${encoded(name, "default-name")}"

  private def encoded(value: StrField, default: String) = value.option().map(_.urlComponentEncoded).getOrElse(default)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Parameter.apply
  override def nameField: Field                                                                 = Name
}

object Parameter {
  def apply(): Parameter = apply(Annotations())

  def apply(ast: YPart): Parameter = apply(Annotations(ast))

  def apply(annotations: Annotations): Parameter = new Parameter(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Parameter = new Parameter(fields, annotations)
}
