package amf.client.`new`

import amf.core.model.domain.DomainElement
import amf.plugins.document.vocabularies.emitters.common.IdCounter

trait AmfIdGenerator {

  // get id or set id?
  def id(d: DomainElement, baseUri: String): String

}

// how get parent?
object PathAmfIdGenerator extends AmfIdGenerator {
  override def id(d: DomainElement, baseUri: String): String = {
    baseUri + "/" + d.componentId
  }
}

class AutoIncrementAmfIdGenerator() extends AmfIdGenerator {

  // check order for test?

  private val idCounter = new IdCounter()
  override def id(d: DomainElement, baseUri: String): String = {
    idCounter.genId("file://")
  }
}
