package amf.plugins.render

import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.plugins.{NormalPriority, PluginPriority}
import amf.client.remod.amfcore.plugins.render.{RenderConfiguration, RenderInfo, StringDocBuilder}
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote.{Proto3, Syntax, Vendor}
import amf.plugins.document.apicontract.parser.spec.oas.GrpcDocumentEmitter
import org.yaml.model.YDocument

object GrpcRenderPlugin extends ApiRenderPlugin {
  override def vendor: Vendor = Vendor.PROTO3

  override protected def unparseAsYDocument(unit: BaseUnit, renderOptions: RenderOptions, errorHandler: AMFErrorHandler): Option[YDocument] = throw new Exception("Cannot geenrate GRPC as a YAML document")

  override def emitString(unit: BaseUnit, builder: StringDocBuilder, renderConfiguration: RenderConfiguration): Boolean = {
    new GrpcDocumentEmitter(unit, builder).emit()
    true
  }

  override def defaultSyntax(): String = Proto3.mediaType

  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq

  override def applies(element: RenderInfo): Boolean = true

  override def priority: PluginPriority = NormalPriority
}
