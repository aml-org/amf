package amf.model

import amf.model.domain.DomainElement
import amf.plugins.document.vocabularies.model.domain.{DomainEntity => CoreDomainEntity}
import amf.plugins.document.vocabularies.spec.DialectNode

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class DomainEntity(private val entity: CoreDomainEntity) extends DomainElement {

  val definition: DialectNode = entity.definition

  override private[amf] def element: CoreDomainEntity = entity

}
