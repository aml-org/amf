package amf.graphqlfederation.internal.spec.context

import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{GraphQLFederation, Spec}
import amf.graphql.internal.spec.context.{GraphQLBaseSettings, GraphQLBaseWebApiContext}

class GraphQLFederationWebApiContext(
    override val loc: String,
    override val refs: Seq[ParsedReference],
    override val options: ParsingOptions,
    override protected val wrapped: ParserContext,
    override protected val ds: Option[WebApiDeclarations] = None
) extends GraphQLBaseWebApiContext(loc, refs, options, wrapped, GraphQLFederationSettings, ds) {}

object GraphQLFederationSettings extends GraphQLBaseSettings {
  override val spec: Spec = GraphQLFederation
}
