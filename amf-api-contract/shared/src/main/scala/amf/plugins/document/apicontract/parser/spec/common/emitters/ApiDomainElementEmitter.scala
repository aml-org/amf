package amf.plugins.document.apicontract.parser.spec.common.emitters

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.remote.Vendor
import amf.core.internal.render.emitters.DomainElementEmitter
import amf.plugins.document.apicontract.parser.spec.common.emitters.factory.DomainElementEmitterFactory
import amf.validations.RenderSideValidations
import org.yaml.model.YNode

object ApiDomainElementEmitter extends DomainElementEmitter[Vendor] {

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
