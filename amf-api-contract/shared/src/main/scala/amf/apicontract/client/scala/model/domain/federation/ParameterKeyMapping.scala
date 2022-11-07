package amf.apicontract.client.scala.model.domain.federation

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.metamodel.domain.federation.ParameterKeyMappingModel
import amf.apicontract.internal.metamodel.domain.federation.ParameterKeyMappingModel._
import amf.core.client.scala.model.domain.extensions.PropertyShapePath
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.client.scala.model.domain.federation.KeyMapping
import org.yaml.model.YMap

case class ParameterKeyMapping(fields: Fields, annotations: Annotations) extends KeyMapping {

  override type Source     = Parameter
  override type Target     = PropertyShapePath
  override type WithTarget = PropertyShapePath

  override def source: Parameter         = fields.field(Source)
  override def target: PropertyShapePath = fields.field(Target)

  override def withSource(source: Parameter): this.type         = set(Source, source)
  override def withTarget(target: PropertyShapePath): this.type = set(Target, target)

  override def meta: ParameterKeyMappingModel.type = ParameterKeyMappingModel
  override def componentId                         = s"/parameterKeyMapping"
}

object ParameterKeyMapping {
  def apply(): ParameterKeyMapping                         = apply(Annotations())
  def apply(ast: YMap): ParameterKeyMapping                = apply(Annotations(ast))
  def apply(annotations: Annotations): ParameterKeyMapping = ParameterKeyMapping(Fields(), annotations)
}
