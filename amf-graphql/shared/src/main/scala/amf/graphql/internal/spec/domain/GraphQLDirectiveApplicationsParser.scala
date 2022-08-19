package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.DomainElement
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.domain.directives.{
  DeprecatedDirectiveApplicationParser,
  DirectiveApplicationParser,
  RegularDirectiveApplicationParser,
  SpecifiedByDirectiveApplicationParser
}
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{DIRECTIVE, DIRECTIVES}
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationsParser(
    rootNode: Node,
    element: DomainElement,
    directivesPath: Seq[String] = Seq(DIRECTIVES, DIRECTIVE)
)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(parsers: Seq[DirectiveApplicationParser] = GraphQLDirectiveApplicationsParser.defaultParsers): Unit = {
    for {
      directiveNode <- collectNodes(rootNode, directivesPath)
      parser        <- parsers.find(_.appliesTo(directiveNode))
    } yield {
      parser.parse(directiveNode, element)
    }
  }
}

object GraphQLDirectiveApplicationsParser {
  def defaultParsers()(implicit ctx: GraphQLBaseWebApiContext): Seq[DirectiveApplicationParser] =
    Seq(
      new DeprecatedDirectiveApplicationParser,
      new SpecifiedByDirectiveApplicationParser,
      new RegularDirectiveApplicationParser
    )
}
