package amf.apicontract.internal.spec.async

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.apicontract.internal.spec.async.emitters.context.{Async20SpecEmitterContext, AsyncSpecEmitterContext}
import amf.apicontract.internal.spec.async.emitters.document.AsyncApi20DocumentEmitter
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, Fragment, Module}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.plugins.render.RenderInfo
import amf.core.internal.remote.Mimes._
import amf.core.internal.remote.Vendor
import org.yaml.model.YDocument

object Async20RenderPlugin extends ApiRenderPlugin {

  override def vendor: Vendor = Vendor.ASYNC20

  override def priority: PluginPriority = NormalPriority

  override def defaultSyntax(): String = `application/yaml`

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def applies(element: RenderInfo): Boolean = element.unit match {
    case document: Document => document.encodes.isInstanceOf[Api]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => false
        case _                => false
      }
    case _: Fragment => false
    case _           => false
  }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] = {
    unit match {
      case document: Document =>
        Some(new AsyncApi20DocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case _ => None
    }
  }

  private def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): AsyncSpecEmitterContext =
    new Async20SpecEmitterContext(errorHandler, options = options)
}
