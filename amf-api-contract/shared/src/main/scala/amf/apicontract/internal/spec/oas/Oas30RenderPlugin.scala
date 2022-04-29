package amf.apicontract.internal.spec.oas

import amf.core.internal.remote.Mimes._
import amf.apicontract.internal.spec.oas.emitter.context.Oas3SpecEmitterContext
import amf.apicontract.internal.spec.oas.emitter.document.{Oas30ModuleEmitter, Oas3DocumentEmitter, OasFragmentEmitter}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration}
import amf.core.internal.remote.Spec
import org.yaml.model.{YDocument, YNode}

object Oas30RenderPlugin extends OasRenderPlugin {

  override def spec: Spec = Spec.OAS30

  override def mediaTypes: Seq[String] = Seq(`application/json`, `application/yaml`)

  override def defaultSyntax(): String = `application/json`

  override def priority: PluginPriority = NormalPriority

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderConfig: RenderConfiguration,
      errorHandler: AMFErrorHandler
  ): Option[YDocument] =
    unit match {
      case module: Module => Some(Oas30ModuleEmitter(module)(specContext(renderConfig, errorHandler)).emitModule())
      case document: Document =>
        Some(Oas3DocumentEmitter(document)(specContext(renderConfig, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment =>
        Some(new OasFragmentEmitter(fragment)(specContext(renderConfig, errorHandler)).emitFragment())
      case _ => None
    }

  private def specContext(renderConfig: RenderConfiguration, errorHandler: AMFErrorHandler): Oas3SpecEmitterContext =
    new Oas3SpecEmitterContext(errorHandler, config = renderConfig)
}
