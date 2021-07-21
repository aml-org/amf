package amf.plugins.render

import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{RenderConfiguration, RenderInfo}
import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.core.internal.remote.{Proto3, Syntax, Vendor}
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
