package amf.graphql.internal.spec.domain.directives

import amf.core.client.scala.model.domain.DomainElement
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import org.mulesoft.antlrast.ast.Node

trait DirectiveApplicationParser extends GraphQLASTParserHelper {
  implicit val ctx: GraphQLBaseWebApiContext
  def appliesTo(node: Node): Boolean
  def parse(node: Node, element: DomainElement): Unit

  protected def isName(name: String, node: Node): Boolean = findName(node, "AnonymousDirective", "Missing directive name")._1 == name
}
