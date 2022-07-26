package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.{DomainElement, ScalarNode}
import amf.core.client.scala.vocabulary.Namespace.XsdTypes
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.domain.ShapeFederationMetadataParser
import amf.shapes.client.scala.model.domain.ScalarShape
import org.mulesoft.antlrast.ast.{ASTNode, Node, Terminal}

class GraphQLNestedEnumParser(enumTypeDef: Node)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {
  val enum: ScalarShape = ScalarShape(toAnnotations(enumTypeDef)).withDataType(DataType.String)

  def parse(): ScalarShape = {
    parseName()
    parseValues()
    parseDescription(enumTypeDef, enum, enum.meta)
    inFederation { implicit fCtx =>
      ShapeFederationMetadataParser(enumTypeDef, enum, Seq(ENUM_DIRECTIVE, ENUM_FEDERATION_DIRECTIVE)).parse()
    }
    GraphQLDirectiveApplicationParser(enumTypeDef, enum).parse()
    enum
  }

  private def parseName(): Unit = {
    val (name, annotations) = findName(enumTypeDef, "AnonymousEnum", "Missing enumeration type name")
    enum.withName(name, annotations)
  }

  private def parseValues(): Unit = {
    path(enumTypeDef, Seq(ENUM_VALUES_DEFINITION)) match {
      case Some(valuesNode: Node) =>
        val values = collect(valuesNode, Seq(ENUM_VALUE_DEFINITION)) map { case valueDefNode: Node =>
          getEnumValue(valueDefNode) match {
            case Some(value: ScalarNode) =>
              inFederation { implicit fCtx =>
              ShapeFederationMetadataParser(valueDefNode, value, Seq(ENUM_VALUE_DIRECTIVE ,ENUM_VALUE_FEDERATION_DIRECTIVE)).parse()
            }
            parseDirectives(valueDefNode, value)
            value
          case None => ScalarNode()
        }
      }
      enum.withValues(values)case _ => enum.withValues(Seq())
    }
  }

  private def getEnumValue(valueNode: Node): Option[ScalarNode] =
    valueFrom(valueNode, Seq(ENUM_VALUE, NAME))
      .orElse(valueFrom(valueNode, Seq(ENUM_VALUE, NAME, KEYWORD)))

  private def valueFrom(element: ASTNode, pathToValue: Seq[String]): Option[ScalarNode] = {
    path(element, pathToValue) match {
      case Some(n: Node) if hasTerminalChild(n) =>
        val t = n.children.head.asInstanceOf[Terminal]
        val s = ScalarNode(t.value, Some(XsdTypes.xsdString.iri()), toAnnotations(t)).withName(t.value)
        Some(s)
      case _ => None
    }
  }

  private def hasTerminalChild(n: Node) = n.children.head.isInstanceOf[Terminal]

  private def parseDirectives(n: Node, element: DomainElement): Unit =
    GraphQLDirectiveApplicationParser(n, element).parse()
}
