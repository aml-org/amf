package amf.grpc.internal.plugins.render

import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.internal.plugins.syntax.{ASTBuilder, StringDocBuilder}
import amf.core.internal.remote.{Grpc, Syntax}
import amf.grpc.internal.spec.emitter.document.GrpcDocumentEmitter

object GrpcRenderPlugin extends AMFRenderPlugin {

  override def defaultSyntax(): String = Grpc.mediaType

  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq

  override def applies(element: RenderInfo): Boolean = true

  override def priority: PluginPriority = NormalPriority

  override val id: String = "grpc-render-plugin"

  override def emit[T](
      unit: BaseUnit,
      builder: ASTBuilder[T],
      renderConfiguration: RenderConfiguration,
      mediaType: String
  ): Boolean = {
    builder match {
      case stringBuilder: StringDocBuilder =>
        new GrpcDocumentEmitter(unit, stringBuilder).emit()
        true
      case _ => false
    }

  }
  override def getDefaultBuilder: ASTBuilder[_] = new StringDocBuilder()

}
