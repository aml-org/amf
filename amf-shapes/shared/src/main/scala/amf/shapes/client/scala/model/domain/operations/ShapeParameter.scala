package amf.shapes.client.scala.model.domain.operations

import amf.core.client.scala.model.domain.federation.{HasFederationMetadata, ShapeFederationMetadata}
import amf.core.client.scala.model.{BoolField, StrField}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.operations.ShapeParameterModel
import amf.shapes.internal.domain.metamodel.operations.ShapeParameterModel._
import org.yaml.model.YPart

case class ShapeParameter(override val fields: Fields, override val annotations: Annotations)
    extends AbstractParameter(fields, annotations) with HasFederationMetadata[ShapeFederationMetadata] {

  override private[amf] def buildParameter(ann: Annotations): ShapeParameter = ShapeParameter(ann)
  override def parameterName: StrField                                       = fields.field(ParameterName)
  override def required: BoolField                                           = fields.field(Required)
  override def binding: StrField                                             = fields.field(Binding)
  override def withParameterName(name: String, annots: Annotations = Annotations()): this.type =
    set(ParameterName, name, annots)
  override def withRequired(required: Boolean): this.type                  = set(Required, required)
  override def withBinding(binding: String): this.type                     = set(Binding, binding)

  override def meta: ShapeParameterModel.type = ShapeParameterModel
}

object ShapeParameter {
  def apply(): ShapeParameter = apply(Annotations())

  def apply(ast: YPart): ShapeParameter = apply(Annotations(ast))

  def apply(annotations: Annotations): ShapeParameter = new ShapeParameter(Fields(), annotations)
}
