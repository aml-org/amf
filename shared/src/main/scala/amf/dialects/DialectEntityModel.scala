package amf.dialects

import amf.metadata.Field
import amf.metadata.domain.DomainElementModel
import amf.spec.dialect.DomainEntity
import amf.vocabulary.ValueType

/**
  * Created by kor on 13/09/17.
  */
class DialectEntityModel (domainEntity: DomainEntity) extends DomainElementModel with amf.model.DomainElement {
  override val fields: List[Field] = {
    val fl=domainEntity.fields.fieldsMeta()
    var num=0;
    var props=domainEntity.definition._props();
    fl.sortBy(f=>props.indexWhere(p=>p.field==f));
  }
  override val `type`: List[ValueType] = domainEntity.definition.`type`

  def element: amf.domain.DomainElement = domainEntity
}
