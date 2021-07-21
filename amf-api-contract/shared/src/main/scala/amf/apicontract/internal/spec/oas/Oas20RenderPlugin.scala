package amf.apicontract.internal.spec.oas

import amf.apicontract.internal.spec.SyntaxMediaTypes._
import amf.apicontract.internal.spec.oas.emitter.context.{Oas2SpecEmitterContext, OasSpecEmitterContext}
import amf.apicontract.internal.spec.oas.emitter.document.{Oas20ModuleEmitter, Oas2DocumentEmitter, OasFragmentEmitter}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.internal.plugins.render.AMFRenderPlugin.APPLICATION_JSON
import amf.core.internal.remote.Vendor
import org.yaml.model.{YDocument, YNode}

object Oas20RenderPlugin extends OasRenderPlugin {

  override def vendor: Vendor = Vendor.OAS20

  override def defaultSyntax(): String = APPLICATION_JSON

  override def mediaTypes: Seq[String] = Seq(`APPLICATION/JSON`, `APPLICATION/YAML`)

  override def unparseAsYDocument(unit: BaseUnit,
                                  renderOptions: RenderOptions,
                                  errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case module: Module => Some(Oas20ModuleEmitter(module)(specContext(renderOptions, errorHandler)).emitModule())
      case document: Document =>
        Some(Oas2DocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment =>
        Some(new OasFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
      case _ => None
    }

  override def priority: PluginPriority = NormalPriority

  private def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): OasSpecEmitterContext =
    new Oas2SpecEmitterContext(errorHandler, options = options)
}
