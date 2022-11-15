package amf.antlr.internal.plugins.syntax

import amf.core.client.common.{LowPriority, PluginPriority}
import amf.core.internal.remote.Mimes.`application/graphql`
import amf.core.internal.remote.Syntax
import org.mulesoft.antlrast.ast.Parser
import org.mulesoft.antlrast.platform.PlatformGraphQLFederationParser

object GraphQLFederationSyntaxParsePlugin extends BaseAntlrSyntaxParsePlugin {

  override def parser(): Parser = new PlatformGraphQLFederationParser()

  override def mediaTypes: Seq[String] = Syntax.graphQLMimes.toSeq

  override val id: String = "graphql-federation-parse"

  override def applies(element: CharSequence): Boolean = true

  override def priority: PluginPriority = LowPriority

  override def mainMediaType: String = `application/graphql`
}
