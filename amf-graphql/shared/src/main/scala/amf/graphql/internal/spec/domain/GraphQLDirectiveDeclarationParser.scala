package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{ScalarValueParser, GraphQLASTParserHelper, Locations}
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GraphQLDirectiveDeclarationParser(node: Node)(implicit val ctx: GraphQLWebApiContext)
    extends GraphQLASTParserHelper {
  var directive: CustomDomainProperty = CustomDomainProperty(toAnnotations(node))

  def parse(parentId: String): CustomDomainProperty = {
    parseName()
    directive.adopted(parentId)
    parseArguments()
    parseLocations()
    directive
  }

  private def parseName(): Unit = {
    val name = findName(node, "AnonymousDirective", "Missing directive name", directive.id)
    directive.withName(name)
  }

  private def parseArguments(): Unit = {
    val properties: Seq[PropertyShape] = collect(node, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map {
      case argument: Node =>
        parseArgument(argument)
    }
    val schema = NodeShape().withProperties(properties)
    directive.withSchema(schema)
  }

  private def parseArgument(n: Node): PropertyShape = {
    val propertyShape = PropertyShape()
    val name          = findName(n, "AnonymousDirectiveArgument", directive.id, "Missing argument name")
    // can be UnresolvedShape, as its type may not be parsed yet, it will later be resolved
    val argumentType = parseType(n, directive.id)
    propertyShape.withName(name).withRange(argumentType)
    ScalarValueParser.putDefaultValue(n, propertyShape)
  }

  private def parseLocations(): Unit = {
    var domains = Set[String]()
    collect(node, Seq(DIRECTIVE_LOCATIONS, DIRECTIVE_LOCATION, TYPE_SYSTEM_DIRECTIVE_LOCATION)).foreach {
      case graphqlLocation: Node =>
        val domainsFromLocation = getDomains(graphqlLocation).toSet
        domains = domainsFromLocation ++: domains
    }
    directive = directive.withDomain(domains.toSeq)
  }

  private def getDomains(location: Node): Seq[String] = {
    val locationName: String = location.children.head.asInstanceOf[Terminal].value
    Locations.locationToDomain.getOrElse(locationName, Seq())
  }
}
