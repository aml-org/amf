package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{DomainElement, ObjectNode}
import amf.core.internal.parser.domain.Annotations.inferred
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.Locations.locationToDomain
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, DIRECTIVE, DIRECTIVES}
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, ScalarValueParser}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationParser(node: Node, element: DomainElement)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(parentId: String): Unit = {
    getDirectiveNodes map { directive =>
      val directiveApplication = DomainExtension(toAnnotations(node))
      parseName(directive, directiveApplication)
      directiveApplication.adopted(parentId)
      putDefinedBy(directiveApplication)
      parseArguments(directive, directiveApplication)
      directiveApplication
        .withElement(element.id)
      element.withCustomDomainProperty(directiveApplication)
    }
  }

  private def checkDefaultDirective(name: String): Unit = {
    // if a default directive has been used, first add it to declarations
    // 'skip' and 'include' default directives are operation directives, not schema directives, so we don't support them
    name match {
      case "deprecated" if !isDeclared(name) =>
        val directive = CustomDomainProperty(inferred())
        directive.withName(name, inferred())
        directive.withDomain(Seq("FIELD_DEFINITION", "ENUM_VALUE").flatMap(locationToDomain))
        directive.adopted(ctx.rootId + "/directives/")
        val schema = NodeShape()
        val scalar = ScalarShape(inferred()).withDataType(DataType.String)
        val argument = PropertyShape()
          .withName("reason")
          .withRange(scalar)
        schema.withProperties(Seq(argument))
        directive.withSchema(schema)
        ctx.declarations += directive
      case _ => // ignore
    }
  }

  private def parseName(directiveNode: Node, directiveApplication: DomainExtension): Unit = {
    val name = findName(directiveNode, "AnonymousDirective", "Missing directive name", directiveApplication.id)
    checkDefaultDirective(name)
    directiveApplication.withName(name)
  }

  private def parseArguments(directiveNode: Node, directiveApplication: DomainExtension): Unit = {
    // arguments are parsed as the properties of an ObjectNode, which goes in the Extension field in the DomainExtension
    val schema = ObjectNode()
    schema.adopted(directiveApplication.id)
    collect(directiveNode, Seq(ARGUMENTS, ARGUMENT)).foreach { case argument: Node =>
      parseArgument(argument, schema, directiveApplication)
    }
    directiveApplication.withExtension(schema)
  }

  private def parseArgument(n: Node, objectNode: ObjectNode, directiveApplication: DomainExtension): Unit = {
    val name = findName(n, "AnonymousDirectiveArgument", directiveApplication.id, "Missing argument name")
    ScalarValueParser.parseValue(n).map(scalarNode => objectNode.addProperty(name, scalarNode, toAnnotations(n)))
  }

  private def getDirectiveNodes: Seq[Node] = collect(node, Seq(DIRECTIVES, DIRECTIVE)).map(_.asInstanceOf[Node])

  private def putDefinedBy(directiveApplication: DomainExtension): Unit = {
    ctx.findAnnotation(directiveApplication.name.value(), SearchScope.All) match {
      case Some(directiveDeclaration) => directiveApplication.withDefinedBy(directiveDeclaration)
      case None =>
        astError(
          directiveApplication.id,
          s"Directive ${directiveApplication.name} is not declared",
          toAnnotations(node)
        )
    }
  }

  private def isDeclared(directiveName: String): Boolean =
    ctx.findAnnotation(directiveName, SearchScope.All).isDefined

}
