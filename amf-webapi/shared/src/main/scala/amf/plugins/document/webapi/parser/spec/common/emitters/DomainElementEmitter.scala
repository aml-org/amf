package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.emitter.PartEmitter
import amf.core.model.domain.DomainElement
import amf.core.remote.Vendor
import amf.plugins.domain.shapes.models.AnyShape
import amf.plugins.domain.webapi.models.{EndPoint, Response}
import org.yaml.model.{YDocument, YNode}

object DomainElementEmitter {

  def emit(element: DomainElement, vendor: Vendor): YNode = {

    val factory: DomainElementEmitterFactory = DomainElementEmitterFactory(vendor)

    val emitter: Option[PartEmitter] = element match {
      case s: AnyShape => factory.typeEmitter(s)
      case e: Response => factory.responseEmitter(e)
      case _           => None
    }

    emitter
      .map { emitter =>
        YDocument(b => emitter.emit(b)).node
      }
      .getOrElse(YNode.Empty)

  }

}
