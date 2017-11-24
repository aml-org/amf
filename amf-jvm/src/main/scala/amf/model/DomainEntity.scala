package amf.model

import amf.plugins.document.vocabularies.model.domain
import amf.plugins.document.vocabularies.spec.DialectNode

case class DomainEntity(private val entity: domain.DomainEntity) extends DomainElement {

  val definition: DialectNode = entity.definition

  override private[amf] def element: domain.DomainEntity = entity
}
