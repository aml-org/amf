package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.{FederationMetadataParser, ShapeFederationMetadataFactory}
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationInDeclarationParser(node: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(): Unit = {
    retrieveParsedDirective() match {
      case Some(directive) =>
        collectNodes(node, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).foreach { argNode =>
          val argName  = findName_(argNode)
          val argument = getArgument(directive, argName)

          parseArgumentDirectiveApplications(argNode, argument)
        }
      case _ => // unreachable
    }
  }

  private def parseArgumentDirectiveApplications(argNode: Node, argument: PropertyShape): Unit = {
    GraphQLDirectiveApplicationsParser(argNode, argument).parse()
    inFederation { implicit fCtx =>
      FederationMetadataParser(
        argNode,
        argument,
        Seq(INPUT_VALUE_DIRECTIVE, INPUT_FIELD_FEDERATION_DIRECTIVE),
        ShapeFederationMetadataFactory
      ).parse()
      GraphQLDirectiveApplicationsParser(argNode, argument, Seq(INPUT_VALUE_DIRECTIVE, DIRECTIVE)).parse()
    }
  }

  private def getArgument(directive: CustomDomainProperty, argName: String) = {
    directive.schema
      .asInstanceOf[NodeShape]
      .properties
      .find(_.name.value() == argName)
      .get // we know we have this
  }

  private def retrieveParsedDirective(): Option[CustomDomainProperty] = {
    val name = findName_(node)
    ctx.findAnnotation(name, SearchScope.All)
  }

  // we already know we have parsed a name
  private def findName_(n: Node) = findName(n, "unreachable", "unreachable")._1
}
