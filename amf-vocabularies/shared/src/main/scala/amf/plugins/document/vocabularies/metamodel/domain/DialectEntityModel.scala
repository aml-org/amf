package amf.plugins.document.vocabularies.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.DomainElement
import amf.core.parser.Annotations
import amf.plugins.document.vocabularies.model.domain.DomainEntity
import amf.core.vocabulary.ValueType

/**
  *
  */
class DialectEntityModel(domainEntity: DomainEntity) extends DomainElementModel {

  override val fields: List[Field] = {
    val fields = domainEntity.fields.fieldsMeta()
    val props  = domainEntity.definition.props.values.toList

    fields.sortBy { field =>
      props.indexWhere(prop => prop.field == field)
    }
  }

  override val `type`: List[ValueType] = if (domainEntity.definition.hasClazz) domainEntity.definition.`type` else List()

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
