package amf.graphql.internal.spec.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.validation.definitions.ParserSideValidations
import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedDirectiveApplication
import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{AmfScalar, DataNode, DomainElement, ObjectNode}
import amf.core.internal.metamodel.domain.ShapeModel
import amf.core.internal.metamodel.domain.common.{NameFieldSchema, NameFieldShacl}
import amf.core.internal.metamodel.domain.extensions.DomainExtensionModel.DefinedBy
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.parser.domain.Annotations.{inferred, virtual}
import amf.core.internal.parser.domain.SearchScope
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.Locations.locationToDomain
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ARGUMENT, ARGUMENTS, DIRECTIVE, DIRECTIVES}
import amf.graphql.internal.spec.parser.syntax.{GraphQLASTParserHelper, ScalarValueParser}
import amf.graphql.internal.spec.parser.validation.ParsingValidationsHelper.checkDuplicates
import amf.shapes.client.scala.model.domain.operations.{ShapeOperation, ShapeParameter}
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape, UnionShape}
import amf.shapes.internal.domain.metamodel.operations.ShapeParameterModel
import org.mulesoft.antlrast.ast.Node

case class GraphQLDirectiveApplicationParser(
    node: Node,
    element: DomainElement,
    directivesPath: Seq[String] = Seq(DIRECTIVES, DIRECTIVE),
    appliedToDirectiveArgument: Boolean = false
)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  def parse(): Unit = {
    getDirectiveNodes map { directive =>
      val directiveApplication = DomainExtension(toAnnotations(directive))
      parseName(directive, directiveApplication)
      putDefinedBy(directiveApplication)
      checkLocation(directiveApplication, element)
      parseArguments(directive, directiveApplication)
      element.withCustomDomainProperty(directiveApplication)
    }
    checkApplicationsAreUnique()
  }
  private def parseName(directiveNode: Node, directiveApplication: DomainExtension): Unit = {
    val (name, annotations) = findName(directiveNode, "AnonymousDirective", "Missing directive name")
    checkDefaultDirective(name)
    directiveApplication.withName(name, annotations)
  }

  private def parseArguments(directiveNode: Node, directiveApplication: DomainExtension): Unit = {
    // arguments are parsed as the properties of an ObjectNode, which goes in the Extension field in the DomainExtension
    val schema = ObjectNode(virtual())
    collect(directiveNode, Seq(ARGUMENTS, ARGUMENT)).foreach { case argument: Node =>
      parseArgument(argument, schema, directiveApplication)
    }
    directiveApplication.withExtension(schema)
  }

  private def parseArgument(n: Node, objectNode: ObjectNode, directiveApplication: DomainExtension): Unit = {
    val (name, _) = findName(n, "AnonymousDirectiveArgument", "Missing argument name")
    ScalarValueParser.parseValue(n).map(scalarNode => objectNode.addProperty(name, scalarNode, toAnnotations(n)))
  }

  private def getDirectiveNodes: Seq[Node] = collect(node, directivesPath).map(_.asInstanceOf[Node])

  private def putDefinedBy(directiveApplication: DomainExtension): Unit = {
    ctx.findAnnotation(directiveApplication.name.value(), SearchScope.All) match {
      case Some(directiveDeclaration) => directiveApplication.setWithoutId(DefinedBy, directiveDeclaration, inferred())
      case None =>
        astError(
          s"Directive ${directiveApplication.name} is not declared",
          toAnnotations(node)
        )
    }
  }

  private def checkLocation(directiveApplication: DomainExtension, element: DomainElement): Unit = {
    val validDomains   = directiveApplication.definedBy.domain.map(_.toString)
    val currentDomains = element.meta.typeIris // maybe head?

    // when parsing directives we parse arguments as property shapes of a virtual node shape
    lazy val validInDirective = appliedToDirectiveArgument && element.meta
      .isInstanceOf[PropertyShapeModel.type] && validDomains.intersect(ShapeParameterModel.typeIris).nonEmpty

    val message = {
      val kind = element match {
        case _: PropertyShape                      => "field"
        case _: ShapeOperation                     => "field"
        case _: ShapeParameter                     => "argument"
        case s: ScalarShape if s.values.nonEmpty   => "enum"
        case _: ScalarShape                        => "scalar"
        case n: NodeShape if n.isAbstract.value()  => "interface"
        case n: NodeShape if n.isInputOnly.value() => "input object"
        case _: NodeShape                          => "object"
        case _: UnionShape                         => "union"
        case _: DataNode                           => "value"
        case _: EndPoint                           => "field"
        case _: WebApi                             => "schema"
        case _                                     => "type" // should be unreachable
      }

      val nameOpt: Option[String] = element.fields
        .getValueAsOption(NameFieldSchema.Name)
        .orElse(element.fields.getValueAsOption(NameFieldShacl.Name))
        .flatMap { v =>
          element match {
            case _: WebApi => None
            case _: EndPoint =>
              Some(
                v.value
                  .asInstanceOf[AmfScalar]
                  .toString
                  .stripPrefix("Query.")
                  .stripPrefix("Mutation.")
                  .stripPrefix("Subscription.")
              )
            case _ => Some(v.value.asInstanceOf[AmfScalar].toString)
          }
        }
        .map(name => s"'$name'")

      nameOpt match {
        case Some(name) => s"Directive ${directiveApplication.name.value()} cannot be applied to $kind $name"
        case _          => s"Directive ${directiveApplication.name.value()} cannot be applied to $kind"
      }
    }

    if (!currentDomains.exists(validDomains.contains) && !validInDirective)
      ctx.eh.violation(
        ParserSideValidations.InvalidDirectiveApplication,
        directiveApplication,
        None,
        message,
        directiveApplication.position(),
        directiveApplication.location()
      )
  }

  private def checkDefaultDirective(name: String): Unit = {
    // if a default directive has been used, first add it to declarations
    // 'skip' and 'include' default directives are operation directives, not schema directives, so we don't support them
    name match {
      case "deprecated" if !isDeclared(name) =>
        val directive = CustomDomainProperty(inferred())
        directive.withName(name, inferred())
        directive.withDomain(Seq("FIELD_DEFINITION", "ENUM_VALUE").flatMap(locationToDomain))
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

  private def isDeclared(directiveName: String): Boolean =
    ctx.findAnnotation(directiveName, SearchScope.All).isDefined

  private def checkApplicationsAreUnique(): Unit = {
    checkDuplicates(element.customDomainProperties, DuplicatedDirectiveApplication, duplicatedApplicationMsg)
  }

  private def duplicatedApplicationMsg(directiveName: String): String =
    s"Directive '$directiveName' can only be applied once per location"
}
