package amf.grpc.internal.plugins.render

import amf.apicontract.internal.plugins.ApiRenderPlugin
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.parse.document.ParsedDocument
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.internal.plugins.syntax.{ASTBuilder, StringDocBuilder}
import amf.core.internal.remote.Spec.GRPC
import amf.core.internal.remote.{Grpc, Spec, Syntax}
import amf.grpc.internal.spec.emitter.document.GrpcDocumentEmitter
import org.yaml.builder.DocBuilder
import org.yaml.model.YDocument

object GrpcRenderPlugin extends AMFRenderPlugin {

  override def defaultSyntax(): String = Grpc.mediaType

  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq

  override def applies(element: RenderInfo): Boolean = true

  override def priority: PluginPriority = NormalPriority

  override val id: String = "grpc-render-plugin"

  override def emit[T](unit: BaseUnit, builder: ASTBuilder[T], renderConfiguration: RenderConfiguration): Boolean = {
    builder match {
      case stringBuilder: StringDocBuilder =>
        new GrpcDocumentEmitter(unit, stringBuilder).emit()
        true
      case _ => false
    }

  }
  override def getDefaultBuilder: ASTBuilder[_] = new StringDocBuilder()

}
