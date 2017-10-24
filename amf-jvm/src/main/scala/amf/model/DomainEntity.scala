package amf.model

import amf.spec.dialects.DialectNode

case class DomainEntity(private val entity: amf.domain.dialects.DomainEntity) extends DomainElement {

  val definition: DialectNode = entity.definition

  override private[amf] def element: amf.domain.dialects.DomainEntity = entity
}
