package amf.plugins.render

import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.plugins.render.AMFRenderPlugin.APPLICATION_JSON
import amf.client.remod.amfcore.plugins.{NormalPriority, PluginPriority}
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.{BaseUnit, Document, ExternalFragment, Fragment, Module}
import amf.core.remote.Vendor
import amf.plugins.common.Oas20MediaTypes
import amf.plugins.document.apicontract.contexts.emitter.oas.{Oas2SpecEmitterContext, OasSpecEmitterContext}
import amf.plugins.document.apicontract.parser.spec.oas.{Oas20ModuleEmitter, Oas2DocumentEmitter, OasFragmentEmitter}
import org.yaml.model.{YDocument, YNode}

object Oas20RenderPlugin extends OasRenderPlugin {

  override def vendor: Vendor = Vendor.OAS20

  override def defaultSyntax(): String = APPLICATION_JSON

  override def mediaTypes: Seq[String] = Oas20MediaTypes.mediaTypes

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
    new Oas2SpecEmitterContext(errorHandler, compactEmission = options.isWithCompactedEmission)
}
