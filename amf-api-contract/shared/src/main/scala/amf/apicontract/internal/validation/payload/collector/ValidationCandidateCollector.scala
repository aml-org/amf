package amf.apicontract.internal.validation.payload.collector

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.AmfElement
import amf.core.client.scala.traversal.iterator.{AmfElementStrategy, InstanceCollector}
import amf.core.internal.validation.ValidationCandidate

trait ValidationCandidateCollector {
  def collect(element: AmfElement): Seq[ValidationCandidate]
}

case class CollectorsRunner(collectors: Seq[ValidationCandidateCollector]) {

  def traverse(unit: BaseUnit): Iterator[ValidationCandidate] =
    for {
      element   <- unit.iterator(strategy = AmfElementStrategy)
      collector <- collectors
      candidate <- collector.collect(element)
    } yield candidate

}
