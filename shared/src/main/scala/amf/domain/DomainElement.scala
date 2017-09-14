package amf.domain

import amf.domain.extensions.DomainExtension
import amf.metadata.Field
import amf.metadata.domain.DomainElementModel.{CustomDomainProperties}
import amf.model.AmfObject
import amf.vocabulary.ValueType

/**
  * Internal model for any domain element
  */
trait DomainElement extends AmfObject {
  def customDomainProperties: Seq[DomainExtension] = fields(CustomDomainProperties)
  def withCustomDomainProperties(customProperties: Seq[DomainExtension]) = setArray(CustomDomainProperties, customProperties)
}

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]
}
