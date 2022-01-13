package amf.graphql.plugins.render

import amf.core.internal.remote.{GraphQL, Syntax}
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.plugins.render.{AMFRenderPlugin, RenderConfiguration, RenderInfo}
import amf.core.internal.plugins.syntax.{ASTBuilder, StringDocBuilder}
import amf.graphql.internal.spec.emitter.document.GraphQLDocumentEmitter

object GraphQLRenderPlugin extends AMFRenderPlugin {
  override def defaultSyntax(): String = GraphQL.mediaType

  override def emit[T](unit: BaseUnit, builder: ASTBuilder[T], renderConfiguration: RenderConfiguration): Boolean = {
    builder match {
      case stringBuilder: StringDocBuilder =>
        new GraphQLDocumentEmitter(unit, stringBuilder).emit()
        true
      case _ => false
    }

  }

  override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq

  override def getDefaultBuilder: ASTBuilder[_] = new StringDocBuilder()

  override val id: String = "graphql-render-plugin"

  override def applies(element: RenderInfo): Boolean = true

  override def priority: PluginPriority = NormalPriority
}
