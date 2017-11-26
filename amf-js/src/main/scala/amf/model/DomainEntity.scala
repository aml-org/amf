package amf.model.domain;

case class DomainEntity(private val entity: domain.DomainEntity) extends DomainElement {

  val definition: DialectNode = entity.definition

  override private[amf] def element: domain.DomainEntity = entity
}
