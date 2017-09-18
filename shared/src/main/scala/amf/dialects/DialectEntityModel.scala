package amf.dialects

import amf.domain.Annotations
import amf.metadata.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.ValueType

/**
  * Created by Pavel Petrochenko on 13/09/17.
  */
class DialectEntityModel (domainEntity: DomainEntity) extends DomainElementModel {


  override val fields: List[Field] = {
    val fields = domainEntity.fields.fieldsMeta()
    val props = domainEntity.definition.props.values.toList

    fields.sortBy { field =>
      props.indexWhere(prop => prop.field == field)
    }
  }

  override val `type`: List[ValueType] = domainEntity.definition.`type`

  def element: amf.domain.DomainElement = domainEntity

  override val dynamicType: Boolean = true

  def adopted(parent: String): this.type = {
    domainEntity.adopted(parent)
    this
  }

  val annotations: Annotations = domainEntity.annotations

}
