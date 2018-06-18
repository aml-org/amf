package amf.core.model.domain

import amf.core.metamodel.Field
import amf.core.parser.Value
import amf.core.vocabulary.ValueType

trait DynamicDomainElement extends DomainElement {
  def dynamicFields: List[Field]
  def dynamicType: List[ValueType]

  override def dynamicTypes(): Seq[String] = dynamicType.map(_.iri())

  // this is used to generate the graph
  def valueForField(f: Field): Option[Value]
}
