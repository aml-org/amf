package amf.apicontract.internal.spec.oas

import amf.apicontract.client.scala.model.document.{Extension, Overlay}
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.core.client.scala.model.document.{Document, Fragment, Module}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.plugins.render.RenderInfo

trait OasRenderPlugin extends ApiRenderPlugin {

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[Api]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
    case _: Fragment => true
    case _           => false
  }
}
