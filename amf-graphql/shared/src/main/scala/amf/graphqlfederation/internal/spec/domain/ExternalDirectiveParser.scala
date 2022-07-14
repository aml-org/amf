package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{
  EXTERNAL_DIRECTIVE,
  FIELD_DIRECTIVE,
  FIELD_FEDERATION_DIRECTIVE,
  INPUT_FIELD_FEDERATION_DIRECTIVE,
  INPUT_VALUE_DIRECTIVE
}
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import org.mulesoft.antlrast.ast.Node

case class ExternalDirectiveParser(ast: Node, target: PropertyShape)(implicit val ctx: GraphQLFederationWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(): Unit = {
    pathToNonTerminal(ast, Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE, EXTERNAL_DIRECTIVE))
      .foreach { _ =>
        target.withIsStub(true)
      }

    pathToNonTerminal(ast, Seq(INPUT_VALUE_DIRECTIVE, INPUT_FIELD_FEDERATION_DIRECTIVE, EXTERNAL_DIRECTIVE))
      .foreach { _ =>
        target.withIsStub(true)
      }
  }
}
