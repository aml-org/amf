package amf.shapes.client.scala.model.domain.federation

import amf.core.client.scala.model.BoolField
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.PropertyShapePath
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.shapes.internal.domain.metamodel.federation.KeyModel
import amf.shapes.internal.domain.metamodel.federation.KeyModel._
import org.yaml.model.YMap

case class Key(fields: Fields, annotations: Annotations) extends DomainElement {

  def components: Seq[PropertyShapePath]                            = fields.field(Components)
  def withComponents(components: Seq[PropertyShapePath]): this.type = setArray(Components, components)

  def isResolvable: BoolField                        = fields.field(IsResolvable)
  def withResolvable(resolvable: Boolean): this.type = set(IsResolvable, resolvable)

  override def meta: KeyModel.type      = KeyModel
  override private[amf] def componentId = s"/key"
}

object Key {
  def apply(): Key                         = apply(Annotations())
  def apply(ast: YMap): Key                = apply(Annotations(ast))
  def apply(annotations: Annotations): Key = Key(Fields(), annotations)
}
