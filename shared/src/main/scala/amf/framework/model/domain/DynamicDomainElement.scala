package amf.framework.model.domain

import amf.framework.metamodel.Field
import amf.vocabulary.ValueType

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]

  // this is used to generate the graph
  def valueForField(f: Field): Option[AmfElement]
}
