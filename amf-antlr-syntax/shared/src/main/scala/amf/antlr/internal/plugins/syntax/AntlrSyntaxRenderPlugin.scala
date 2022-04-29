package amf.antlr.internal.plugins.syntax

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.core.client.common.{NormalPriority, PluginPriority}
import amf.core.client.scala.parse.document.{ParsedDocument, StringParsedDocument}
import amf.core.client.scala.render.AMFSyntaxRenderPlugin
import amf.core.internal.remote.Syntax
import org.mulesoft.common.io.Output
import org.mulesoft.common.io.Output.OutputOps

object AntlrSyntaxRenderPlugin extends AMFSyntaxRenderPlugin {

  override def emit[W: Output](mediaType: String, ast: ParsedDocument, writer: W): Option[W] = {
    ast match {
      case str: StringParsedDocument =>
        writer.append(str.ast.builder.toString)
        Some(writer)
      case _ => None
    }
  }

  /** media types which specifies vendors that are parsed by this plugin.
    */
  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq ++ Syntax.graphQLMimes.toSeq

  override val id: String = "antlr-ast-render"

  override def applies(element: ParsedDocument): Boolean = element.isInstanceOf[StringParsedDocument]

  override def priority: PluginPriority = NormalPriority
}
