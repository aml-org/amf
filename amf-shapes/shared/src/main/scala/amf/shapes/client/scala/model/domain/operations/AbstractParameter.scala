package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.core.internal.utils.AmfStrings
import amf.shapes.client.scala.model.domain.{ExemplifiedDomainElement, NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractParameterModel
import amf.shapes.internal.domain.metamodel.`abstract`.AbstractParameterModel.{Binding, Description, ParameterName, Required, Schema}
import org.yaml.model.YPart

/**
  * ShapeParameter internal model.
  */
abstract class AbstractParameter(override val fields: Fields, override val annotations: Annotations)
  extends NamedDomainElement {

  def parameterName: StrField    = fields.field(AbstractParameterModel.ParameterName)
  def description: StrField      = fields.field(Description)
  def required: BoolField        = fields.field(Required)
  def schema: Shape              = fields.field(Schema)
  def binding: StrField          = fields.field(Binding)

  def withBinding(binding: String): this.type = set(Binding, binding)
  def withParameterName(name: String, annots: Annotations = Annotations()): this.type =
    set(ParameterName, name, annots)
  def withDescription(description: String): this.type          = set(Description, description)
  def withRequired(required: Boolean): this.type               = set(Required, required)
  def withSchema(schema: Shape): this.type            = set(Schema, schema)


  def withObjectSchema(name: String): NodeShape = {
    val node = NodeShape().withName(name)
    set(AbstractParameterModel.Schema, node)
    node
  }

  def withScalarSchema(name: String): ScalarShape = {
    val scalar = ScalarShape().withName(name)
    set(AbstractParameterModel.Schema, scalar, Annotations.synthesized())
    scalar
  }

  protected def buildParameter(ann:Annotations) : AbstractParameter

  def cloneParameter(parent: String): AbstractParameter = {
    val parameter: AbstractParameter = buildParameter(Annotations(annotations))
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

  override def meta: DomainElementModel = AbstractParameterModel

  /** Value , path + field value that is used to compose the id when the object its adopted */
  private[amf] override def componentId: String =
    s"/parameter/${encoded(name, "default-name")}"

  private def encoded(value: StrField, default: String) = value.option().map(_.urlComponentEncoded).getOrElse(default)

  override def nameField: Field  = AbstractParameterModel.Name
}

