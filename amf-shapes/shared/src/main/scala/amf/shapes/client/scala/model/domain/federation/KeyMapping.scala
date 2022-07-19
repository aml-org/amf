package amf.shapes.client.scala.model.domain.federation

import amf.core.client.scala.model.domain.DomainElement
import amf.shapes.internal.domain.metamodel.federation.KeyMappingModel

trait KeyMapping extends DomainElement {
  type Source
  type Target

  override def meta: KeyMappingModel

  def source: Source
  def target: Target

  def withSource(source: Source): this.type
  def withTarget(target: Target): this.type
}
