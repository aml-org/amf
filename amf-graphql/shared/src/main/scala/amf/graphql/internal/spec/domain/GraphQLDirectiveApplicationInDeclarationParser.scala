package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationInDeclarationParser(node: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(): Unit = {
    retrieveParsedDirective() match {
      case Some(directive) =>
        collectNodes(node, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach { arg =>
          val argName = findName_(arg)
          val element = directive.schema.asInstanceOf[NodeShape].properties.find(_.name.value() == argName).get // we know we have this
          GraphQLDirectiveApplicationParser(arg, element, appliedToDirectiveArgument = true).parse()
        }
      case _ => // unreachable
    }
  }

  private def retrieveParsedDirective(): Option[CustomDomainProperty] = {
    val name = findName_(node)
    ctx.findAnnotation(name, SearchScope.All)
  }

  // we already know we have parsed a name
  private def findName_(n: Node) = findName(n, "unreachable", "unreachable")._1
}
