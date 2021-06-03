package amf.plugins.document.apicontract.validation.collector

import amf.core.model.document.BaseUnit
import amf.core.model.domain.AmfElement
import amf.core.traversal.iterator.AmfElementStrategy
import amf.core.validation.ValidationCandidate

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
