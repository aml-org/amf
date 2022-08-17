package amf.graphql.internal.spec.domain

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.internal.parser.domain.Annotations.virtual
import amf.graphql.internal.spec.annotations.GraphQLLocation
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, Locations}
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.internal.annotations.DirectiveArguments
import org.mulesoft.antlrast.ast.{Error, Node, Terminal}
import org.mulesoft.common.client.lexical.ASTElement

case class GraphQLDirectiveDeclarationParser(node: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val directive: CustomDomainProperty = CustomDomainProperty(toAnnotations(node))

  def parse(): CustomDomainProperty = {
    parseName()
    parseRepeatable()
    parseArguments()
    parseLocations()
    parseDescription(node, directive, directive.meta)
    directive
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(node, "AnonymousDirective", "Missing directive name")
    directive.withName(name, annotations)
  }

  def parseRepeatable(): Unit = {
    if (pathToTerminal(node, Seq("'repeatable'")).isDefined) {
      directive.withRepeatable(true)
    }
  }

  private def parseArguments(): Unit = {
    val properties: Seq[PropertyShape] = collect(node, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map {
      case argument: Node =>
        parseArgument(argument)
    }
    val schema = NodeShape(virtual())
    schema.withIsInputOnly(true)
    schema.withProperties(properties)
    schema.annotations += DirectiveArguments()
    directive.withSchema(schema)
  }

  private def parseArgument(n: Node): PropertyShape = {
    val propertyShape       = PropertyShape(toAnnotations(n))
    val (name, annotations) = findName(n, "AnonymousDirectiveArgument", "Missing argument name")
    propertyShape.withName(name, annotations)
    // can be UnresolvedShape, as its type may not be parsed yet, it will later be resolved
    val argumentType = parseType(n)
    setDefaultValue(n, propertyShape)
    propertyShape.annotations += DirectiveArguments()
    propertyShape.withRange(argumentType)
  }

  private def parseLocations(): Unit = {
    var domains   = Set[String]()
    var locations = Set[String]()
    collect(node, Seq(DIRECTIVE_LOCATIONS, DIRECTIVE_LOCATION)).foreach { n =>
      path(n, Seq(TYPE_SYSTEM_DIRECTIVE_LOCATION)) match {
        case Some(graphqlLocation: Node) =>
          val locationName = graphqlLocation.children.head.asInstanceOf[Terminal].value
          locations = locations + locationName
          val domainsFromLocation = getDomains(locationName).toSet
          domains = domainsFromLocation ++: domains
        case _ =>
          n match {
            case n: Node => checkErrorNode(n.children.toList)
            case _       => // ignore
          }
      }
    }
    directive.add(GraphQLLocation(locations))
    directive.withDomain(domains.toSeq)
  }

  private def getDomains(locationName: String): Seq[String] = Locations.locationToDomain.getOrElse(locationName, Seq())

  private def checkErrorNode(children: Seq[ASTElement]): Unit = {
    children.foreach {
      case errorNode: Error =>
        ctx.eh.violation(
          ParserSideValidations.InvalidDirectiveLocation,
          directive,
          s"Directive location ${errorNode.message} is invalid",
          directive.annotations
        )
      case _ => // ignore
    }
  }
}
