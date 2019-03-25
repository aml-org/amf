package amf.core.model.domain

import amf.core.metamodel.Field
import amf.core.parser.Value
import amf.core.vocabulary.ValueType

trait DynamicDomainElement extends DomainElement {
  // this is used to generate the graph
  def valueForField(f: Field): Option[Value] // TODO!
}
