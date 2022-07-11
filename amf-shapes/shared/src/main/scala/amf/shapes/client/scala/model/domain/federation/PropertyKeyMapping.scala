package amf.shapes.client.scala.model.domain.federation

import amf.core.client.scala.model.domain.extensions
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.federation.PropertyKeyMappingModel
import org.yaml.model.YMap

case class PropertyKeyMapping(fields: Fields, annotations: Annotations) extends KeyMapping {

  override type Source = PropertyShape
  override type Target = String

  override def meta: PropertyKeyMappingModel.type = PropertyKeyMappingModel
  override private[amf] def componentId           = s"/parameterKeyMapping"

  override def source: extensions.PropertyShape = fields.field(PropertyKeyMappingModel.Source)
  override def target: String                   = fields.field(PropertyKeyMappingModel.Target)

  override def withSource(source: extensions.PropertyShape): this.type = set(PropertyKeyMappingModel.Source, source)
  override def withTarget(target: String): this.type                   = set(PropertyKeyMappingModel.Target, target)

}

object PropertyKeyMapping {
  def apply(): PropertyKeyMapping                         = apply(Annotations())
  def apply(ast: YMap): PropertyKeyMapping                = apply(Annotations(ast))
  def apply(annotations: Annotations): PropertyKeyMapping = PropertyKeyMapping(Fields(), annotations)
}
