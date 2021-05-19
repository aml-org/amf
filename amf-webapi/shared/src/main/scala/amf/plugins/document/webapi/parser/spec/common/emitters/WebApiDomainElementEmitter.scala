package amf.plugins.document.webapi.parser.spec.common.emitters

import amf.core.emitter.DomainElementEmitter
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.model.domain.DomainElement
import amf.core.remote.Vendor
import amf.plugins.document.webapi.parser.spec.common.emitters.factory.DomainElementEmitterFactory
import amf.validations.RenderSideValidations
import org.yaml.model.YNode

object WebApiDomainElementEmitter extends DomainElementEmitter[Vendor] {

  /**
    * @param references : optional parameter that is not used in webapi element emitter
    */
  override def emit(element: DomainElement,
                    emissionStructure: Vendor,
                    eh: AMFErrorHandler,
                    references: Seq[BaseUnit] = Nil): YNode = {
    DomainElementEmitterFactory(emissionStructure, eh) match {
      case Some(factory) =>
        val emitter = factory.emitter(element)
        nodeOrError(emitter, element.id, eh)
      case None =>
        eh.violation(RenderSideValidations.UnknownVendor, element.id, "Unknown vendor provided")
        YNode.Empty
    }
  }

}
