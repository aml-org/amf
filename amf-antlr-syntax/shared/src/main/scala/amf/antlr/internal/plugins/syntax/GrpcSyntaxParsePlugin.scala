package amf.antlr.internal.plugins.syntax

import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.internal.remote.Mimes.`application/x-protobuf`
import amf.core.internal.remote.Syntax
import org.mulesoft.antlrast.ast.Parser
import org.mulesoft.antlrast.platform.PlatformProtobuf3Parser

object GrpcSyntaxParsePlugin extends BaseAntlrSyntaxParsePlugin {

  override def mediaTypes: Seq[String] = Syntax.proto3Mimes.toSeq

  override val id: String = "grpc-parse"

  override def applies(element: CharSequence): Boolean = true

  override def priority: PluginPriority = LowPriority

  override def mainMediaType: String = `application/x-protobuf`

  override def parser(): Parser = new PlatformProtobuf3Parser()
}
