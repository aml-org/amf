package amf.graphql.internal.spec.domain

import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.shacl.graphql.GraphQLLocationHelper.toLocationIri
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, PropertyShape}
import amf.core.internal.parser.domain.Annotations.virtual
import amf.core.internal.remote.{GraphQL, GraphQLFederation}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
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
    val tokenName = ctx.spec match {
      case GraphQL           => "'repeatable'"
      case GraphQLFederation => "REPEATABLE_KEYWORD"
    }
    if (pathToTerminal(node, Seq(tokenName)).isDefined) {
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
    parseDescription(n, propertyShape, propertyShape.meta)
    // can be UnresolvedShape, as its type may not be parsed yet, it will later be resolved
    val argumentType = parseType(n)
    setDefaultValue(n, propertyShape)
    propertyShape.annotations += DirectiveArguments()
    propertyShape.withRange(argumentType)
  }

  private def parseLocations(): Unit = {
    var domains = Seq[String]()
    collect(node, Seq(DIRECTIVE_LOCATIONS, DIRECTIVE_LOCATION)).foreach { n =>
      lazy val typeSystemLocation = path(n, Seq(TYPE_SYSTEM_DIRECTIVE_LOCATION))
      lazy val executableLocation = path(n, Seq(EXECUTABLE_DIRECTIVE_LOCATION))
      typeSystemLocation.orElse(executableLocation) match {
        case Some(rawLocation: Node) =>
          val locationName = rawLocation.children.head.asInstanceOf[Terminal].value
          toLocationIri(locationName) match {
            case Some(locationIri) =>
              domains = domains :+ locationIri
            case None => // unreachable, will fail first on ANTLR parsing
          }
        case _ =>
          n match {
            case n: Node => checkErrorNode(n.children.toList)
            case _       => // ignore
          }
      }
    }
    directive.withDomain(domains.distinct)
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
}
