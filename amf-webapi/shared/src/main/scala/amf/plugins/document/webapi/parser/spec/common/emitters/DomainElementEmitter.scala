package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.emitter.PartEmitter
import amf.core.errorhandling.ErrorHandler
import amf.core.model.domain.DomainElement
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.common.emitters.factory.DomainElementEmitterFactory
import amf.validations.RenderSideValidations
import org.yaml.model.{YDocument, YNode}

object DomainElementEmitter {

  def emit(element: DomainElement, vendor: Vendor, eh: ErrorHandler): YNode = {
    DomainElementEmitterFactory(vendor, eh) match {
      case Some(factory) =>
        val emitter = factory.emitter(element)
        nodeOrError(emitter, element.id, eh)
      case None =>
        eh.violation(RenderSideValidations.UnknownVendor, element.id, "Unknown vendor provided")
        YNode.Empty
    }
  }

  private def nodeOrError(emitter: Option[PartEmitter], id: String, eh: ErrorHandler): YNode = {
    emitter
      .map { emitter =>
        YDocument(b => emitter.emit(b)).node
      }
      .getOrElse {
        eh.violation(RenderSideValidations.UnhandledDomainElement, id, "Unhandled domain element for given vendor")
        YNode.Empty
      }
  }
}
