package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.model.domain.DomainElement
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.common.emitters.factory.DomainElementEmitterFactory
import org.yaml.model.{YDocument, YNode}

object DomainElementEmitter {

  def emit(element: DomainElement, vendor: Vendor): YNode = {
    val factory = DomainElementEmitterFactory(vendor)
    val emitter = factory.flatMap(_.emitter(element))
    emitter
      .map { emitter =>
        YDocument(b => emitter.emit(b)).node
      }
      .getOrElse(YNode.Empty)
  }

}
