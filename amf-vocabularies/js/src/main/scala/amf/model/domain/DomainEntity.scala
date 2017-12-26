package amf.model.domain

import amf.plugins.document.vocabularies.model.domain.{DomainEntity => CoreDomainEntity}
import amf.plugins.document.vocabularies.spec.DialectNode

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportTopLevel("model.domain.DomainEntity")
@JSExportAll
class DomainEntity(private val entity: CoreDomainEntity) extends DomainElement {

  val definition: DialectNode = entity.definition

  override private[amf] def element: CoreDomainEntity = entity

}
