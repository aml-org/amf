package amf.apicontract.internal.validation.shacl.graphql

import amf.core.client.scala.model.domain.AmfElement
import amf.core.client.scala.traversal.iterator.{
  AmfIterator,
  DomainElementIterator,
  IdCollector,
  IteratorStrategy,
  VisitedCollector
}

object GraphQLIteratorStrategy extends IteratorStrategy {
  override def iterator(elements: List[AmfElement], visited: VisitedCollector = IdCollector()): AmfIterator =
    DomainElementIterator.withFilter(elements, visited, GraphQLFieldsFilter)
}
