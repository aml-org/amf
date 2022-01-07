package amf.apicontract.internal.spec.oas

import amf.core.internal.remote.Mimes._
import amf.apicontract.internal.spec.oas.emitter.context.{Oas2SpecEmitterContext, OasSpecEmitterContext}
import amf.apicontract.internal.spec.oas.emitter.document.{Oas20ModuleEmitter, Oas2DocumentEmitter, OasFragmentEmitter}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.internal.plugins.render.RenderConfiguration
import amf.core.internal.remote.Spec
import org.yaml.model.{YDocument, YNode}

object Oas20RenderPlugin extends OasRenderPlugin {

  override def spec: Spec = Spec.OAS20

  override def defaultSyntax(): String = `application/json`

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def unparseAsYDocument(unit: BaseUnit,
                                  renderConfig: RenderConfiguration,
                                  errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case module: Module => Some(Oas20ModuleEmitter(module)(specContext(renderConfig, errorHandler)).emitModule())
      case document: Document =>
        Some(Oas2DocumentEmitter(document)(specContext(renderConfig, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment =>
        Some(new OasFragmentEmitter(fragment)(specContext(renderConfig, errorHandler)).emitFragment())
      case _ => None
    }

  override def priority: PluginPriority = NormalPriority

  private def specContext(renderConfig: RenderConfiguration, errorHandler: AMFErrorHandler): OasSpecEmitterContext =
    new Oas2SpecEmitterContext(errorHandler, config = renderConfig)
}
