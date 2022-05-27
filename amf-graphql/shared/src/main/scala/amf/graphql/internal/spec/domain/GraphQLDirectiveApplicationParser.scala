package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension}
import amf.core.client.scala.model.domain.{DomainElement, ObjectNode}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, DIRECTIVE, DIRECTIVES}
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, ScalarValueParser}
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationParser(node: Node, element: DomainElement)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  val directiveApplication: DomainExtension = DomainExtension(toAnnotations(node))

  def parse(parentId: String): Unit = {
    getDirectiveNode map { directive =>
      parseName(directive)
      directiveApplication.adopted(parentId)
      putDefinedBy()
      parseArguments(directive)
      directiveApplication
        .withElement(element.id)
      element.withCustomDomainProperty(directiveApplication)
    }
  }

  private def parseName(directiveNode: Node): Unit = {
    val name = findName(directiveNode, "AnonymousDirective", "Missing directive name", directiveApplication.id)
    directiveApplication.withName(name)
  }

  private def parseArguments(directiveNode: Node): Unit = {
    // arguments are parsed as the properties of an ObjectNode, which goes in the Extension field in the DomainExtension
    val schema = ObjectNode()
    collect(directiveNode, Seq(ARGUMENTS, ARGUMENT)).foreach { case argument: Node =>
      parseArgument(argument, schema)
    }
    directiveApplication.withExtension(schema)
  }

  private def parseArgument(n: Node, objectNode: ObjectNode): Unit = {
    val name = findName(n, "AnonymousDirectiveArgument", directiveApplication.id, "Missing argument name")
    ScalarValueParser.parseValue(n).map(scalarNode => objectNode.addProperty(name, scalarNode, toAnnotations(n)))
  }

  private def getDirectiveNode: Option[Node] = path(node, Seq(DIRECTIVES, DIRECTIVE)).map(_.asInstanceOf[Node])

  private def putDefinedBy(): Unit = {
    val definedBy = ctx
      .findAnnotation(directiveApplication.name.value(), SearchScope.All)
      .getOrElse(CustomDomainProperty())
    directiveApplication.withDefinedBy(definedBy)
  }

}
