package amf.client.`new`

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.emitters.common.IdCounter

trait AmfIdGenerator {

  // get id or set id?
  def id(d: DomainElement, parent: String): String

}

// how get parent?
object PathAmfIdGenerator extends AmfIdGenerator {
  override def id(d: DomainElement, parent: String): String = {
    parent + "/" + d.componentId
  }
}

class AutoIncrementAmfIdGenerator() extends AmfIdGenerator {
  private val idCounter = new IdCounter()
  override def id(d: DomainElement, parent: String): String = {
    idCounter.genId("file://")
  }
}
