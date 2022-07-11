package amf.shapes.client.scala.model.domain.federation

import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.federation.ExternalPropertyShapeModel
import amf.shapes.internal.domain.metamodel.federation.ExternalPropertyShapeModel._
import org.yaml.model.YMap

case class ExternalPropertyShape(fields: Fields, annotations: Annotations) extends DomainElement {

  def name: StrField                       = fields.field(Name)
  def keyMappings: Seq[PropertyKeyMapping] = fields.field(KeyMappings)
  def rangeName: StrField                  = fields.field(RangeName)

  def withName(name: String): this.type                                = set(Name, name)
  def withKeyMappings(keyMappings: Seq[PropertyKeyMapping]): this.type = setArray(KeyMappings, keyMappings)
  def withRangeName(rangeName: String): this.type                      = set(RangeName, rangeName)

  override def meta: ExternalPropertyShapeModel.type = ExternalPropertyShapeModel
  override private[amf] def componentId = s"/external-property/${name.option().getOrElse("default-external-property")}"
}

object ExternalPropertyShape {
  def apply(): ExternalPropertyShape                         = apply(Annotations())
  def apply(ast: YMap): ExternalPropertyShape                = apply(Annotations(ast))
  def apply(annotations: Annotations): ExternalPropertyShape = ExternalPropertyShape(Fields(), annotations)
}
