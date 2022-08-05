package amf.graphql.internal.spec.domain

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedArgument
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.internal.parser.domain.Annotations.virtual
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, Locations}
import amf.graphql.internal.spec.parser.validation.ParsingValidationsHelper.checkDuplicates
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.{Error, Node, Terminal}
import org.mulesoft.common.client.lexical.ASTElement

case class GraphQLDirectiveDeclarationParser(node: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val directive: CustomDomainProperty = CustomDomainProperty(toAnnotations(node))

  def parse(): CustomDomainProperty = {
    parseName()
    parseArguments()
    checkArgumentsAreUnique()
    parseLocations()
    parseDescription(node, directive, directive.meta)
    directive
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(node, "AnonymousDirective", "Missing directive name")
    directive.withName(name, annotations)
  }

  private def parseArguments(): Unit = {
    val properties: Seq[PropertyShape] = collect(node, Seq(ARGUMENTS_DEFINITION, INPUT_VALUE_DEFINITION)).map {
      case argument: Node =>
        parseArgument(argument)
    }
    val schema = NodeShape(virtual())
    schema.withIsInputOnly(true)
    schema.withProperties(properties)
    directive.withSchema(schema)
  }

  private def parseArgument(n: Node): PropertyShape = {
    val propertyShape       = PropertyShape(toAnnotations(n))
    val (name, annotations) = findName(n, "AnonymousDirectiveArgument", "Missing argument name")
    propertyShape.withName(name, annotations)
    // can be UnresolvedShape, as its type may not be parsed yet, it will later be resolved
    val argumentType = parseType(n)
    setDefaultValue(n, propertyShape)
    propertyShape.withRange(argumentType)
  }

  private def parseLocations(): Unit = {
    var domains = Set[String]()
    collect(node, Seq(DIRECTIVE_LOCATIONS, DIRECTIVE_LOCATION)).foreach { n =>
      path(n, Seq(TYPE_SYSTEM_DIRECTIVE_LOCATION)) match {
        case Some(graphqlLocation: Node) =>
          val domainsFromLocation = getDomains(graphqlLocation).toSet
          domains = domainsFromLocation ++: domains
        case _ =>
          n match {
            case n: Node => checkErrorNode(n.children.toList)
            case _       => // ignore
          }
      }
    }
    directive.withDomain(domains.toSeq)
  }

  private def getDomains(location: Node): Seq[String] = {
    val locationName: String = location.children.head.asInstanceOf[Terminal].value
    Locations.locationToDomain.getOrElse(locationName, Seq())
  }

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

  private def checkArgumentsAreUnique()(implicit ctx: GraphQLBaseWebApiContext): Unit = {
    val arguments = directive.schema.asInstanceOf[NodeShape].properties
    checkDuplicates(arguments, DuplicatedArgument, duplicatedArgumentMsg)
  }

  private def duplicatedArgumentMsg(argumentName: String): String =
    s"Cannot exist two or more arguments with name '$argumentName'"
}
