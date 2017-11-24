package amf.metadata.domain.dialects

import amf.domain.dialects.DomainEntity
import amf.framework.metamodel.Field
import amf.framework.metamodel.domain.DomainElementModel
import amf.framework.model.domain.DomainElement
import amf.framework.parser.Annotations
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

  def element: DomainElement = domainEntity

  override val dynamicType: Boolean = true

  def adopted(parent: String): this.type = {
    domainEntity.adopted(parent)
    this
  }

  val annotations: Annotations = domainEntity.annotations

  // TODO: Look at this to try to completely split dialects
  override def modelInstance = throw new Exception("Dialect entities cannot be instantiated directly")
}
