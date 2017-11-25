package amf.core.model.domain

import amf.core.metamodel.Field
import amf.core.vocabulary.ValueType

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]

  // this is used to generate the graph
  def valueForField(f: Field): Option[AmfElement]
}
