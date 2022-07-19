package amf.graphql.internal.spec.context

import amf.apicontract.internal.spec.common.WebApiDeclarations
import amf.core.client.scala.config.ParsingOptions
import amf.core.client.scala.parse.document.{ParsedReference, ParserContext}
import amf.core.internal.remote.{GraphQL, Spec}

class GraphQLWebApiContext(
    override val loc: String,
    override val refs: Seq[ParsedReference],
    override val options: ParsingOptions,
    override protected val wrapped: ParserContext,
    override protected val ds: Option[WebApiDeclarations] = None
) extends GraphQLBaseWebApiContext(loc, refs, options, wrapped, GraphQLSettings, ds) {}

object GraphQLSettings extends GraphQLBaseSettings {
  override val spec: Spec = GraphQL
}
