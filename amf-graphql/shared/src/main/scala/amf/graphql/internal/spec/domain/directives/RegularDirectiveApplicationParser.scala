package amf.graphql.internal.spec.domain.directives

import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.model.domain.{DomainElement, ObjectNode}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.parser.domain.Annotations.{inferred, virtual}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.ValueParser
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, VALUE}
import org.mulesoft.antlrast.ast.Node

class RegularDirectiveApplicationParser(override implicit val ctx: GraphQLBaseWebApiContext)
    extends DirectiveApplicationParser {

  override def appliesTo(node: Node): Boolean = true

  def parse(node: Node, element: DomainElement): Unit = {
    val directiveApplication = DomainExtension(toAnnotations(node))
    parseName(directiveApplication, node)
    parseDefinedBy(directiveApplication, node)
    parseArguments(directiveApplication, node)
    element.withCustomDomainProperty(directiveApplication)
  }

  protected def parseName(directiveApplication: DomainExtension, node: Node): Unit = {
    val (name, annotations) = findName(node, "AnonymousDirective", "Missing directive name")
    directiveApplication.withName(name, annotations)
  }

  protected def parseArguments(directiveApplication: DomainExtension, node: Node): Unit = {
    // arguments are parsed as the properties of an ObjectNode, which goes in the Extension field in the DomainExtension
    val schema = ObjectNode(virtual())
    collect(node, Seq(ARGUMENTS, ARGUMENT)).foreach { case argument: Node =>
      parseArgument(argument, schema)
    }
    directiveApplication.withExtension(schema)
  }

  protected def parseArgument(n: Node, objectNode: ObjectNode): Unit = {
    val (name, _) = findName(n, "AnonymousDirectiveArgument", "Missing argument name")
    for {
      valueNode   <- pathToNonTerminal(n, Seq(VALUE))
      parsedValue <- ValueParser.parseValue(valueNode)
    } yield {
      objectNode.addProperty(name, parsedValue, toAnnotations(n))
    }
  }

  protected def parseDefinedBy(directiveApplication: DomainExtension, node: Node): Unit = {
    ctx.findAnnotation(directiveApplication.name.value(), SearchScope.All) match {
      case Some(directiveDeclaration) => directiveApplication.setWithoutId(DefinedBy, directiveDeclaration, inferred())
      case None =>
        astError(
          s"Directive ${directiveApplication.name} is not declared",
          toAnnotations(node)
        )
    }
  }
}
