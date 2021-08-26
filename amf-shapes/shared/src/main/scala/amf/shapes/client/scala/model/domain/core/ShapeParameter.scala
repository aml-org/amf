package amf.shapes.client.scala.model.domain.core

import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement, Linkable, NamedDomainElement, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.ShapeModel.Name
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ExemplifiedDomainElement, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.core.ShapeParameterModel
import amf.shapes.internal.domain.metamodel.core.ShapeParameterModel.{ParameterName, Required, Schema}
import org.yaml.model.YPart

private[amf] class ShapeParameter(override val fields: Fields, override val annotations: Annotations)
    extends NamedDomainElement
    with Linkable
    with ExemplifiedDomainElement {

  def parameterName: StrField = fields.field(ParameterName)
  def required: BoolField     = fields.field(Required)
  def schema: Shape           = fields.field(Schema)

  def withParameterName(name: String, annots: Annotations = Annotations()): this.type =
    set(ParameterName, name, annots)
  def withRequired(required: Boolean): this.type = set(Required, required)
  def withSchema(schema: Shape): this.type       = set(Schema, schema)

  def setSchema(shape: Shape): Shape = {
    set(Schema, shape)
    shape
  }

  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(Schema, scalar, Annotations.synthesized())
    scalar
  }

  override def linkCopy(): ShapeParameter = {
    val copy = ShapeParameter().withId(id)
    copy
  }

  def cloneParameter(parent: String): ShapeParameter = {
    val parameter: ShapeParameter = ShapeParameter(Fields(), Annotations(annotations))
    val cloned                    = parameter.withName(name.value()).adopted(parent)

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

  override def meta: ShapeParameterModel.type = ShapeParameterModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    s"/parameter/default-binding/${encoded(name, "default-name")}"

  private def encoded(value: StrField, default: String) = value.option().map(_.urlComponentEncoded).getOrElse(default)

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ShapeParameter.apply
  override def nameField: Field                                                                 = Name
}

object ShapeParameter {
  def apply(): ShapeParameter          = new ShapeParameter(Fields(), Annotations())
  def apply(f: Fields, a: Annotations) = new ShapeParameter(f, a)
}
