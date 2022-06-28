package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape, ScalarShape, UnionShape}
import org.mulesoft.antlrast.ast.Node

case class GraphQLTypeExtensionParser(typeExtensionDef: Node)(implicit
    val ctx: GraphQLWebApiContext
) extends GraphQLASTParserHelper {

  def parse(): Shape = {
    invokeAppropriateParser()
      .map(_.withIsExtension(true))
      .getOrElse {
        AnyShape()
      }
  }

  private def invokeAppropriateParser(): Option[AnyShape] = {
    this
      .pathToNonTerminal(typeExtensionDef, Seq(SCALAR_TYPE_EXTENSION))
      .map(parseScalarTypeExtension)
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(OBJECT_TYPE_EXTENSION))
          .map(parseObjectTypeExtension)
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(INTERFACE_TYPE_EXTENSION))
          .map(parseInterfaceTypeExtension)
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(UNION_TYPE_EXTENSION))
          .map(parseUnionTypeExtension)
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(ENUM_TYPE_EXTENSION))
          .map(parseEnumTypeExtension)
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(INPUT_OBJECT_TYPE_EXTENSION))
          .map(parseInputObjectTypeExtension)
      }
  }

  def parseScalarTypeExtension(node: Node): ScalarShape = {
    new GraphQLCustomScalarParser(node).parse()
  }

  def parseObjectTypeExtension(node: Node): NodeShape = {
    new GraphQLNestedTypeParser(node).parse()
  }

  def parseInterfaceTypeExtension(node: Node): NodeShape = {
    new GraphQLNestedTypeParser(node, true).parse()
  }

  def parseUnionTypeExtension(node: Node): UnionShape = {
    new GraphQLNestedUnionParser(node).parse()

  }
  def parseEnumTypeExtension(node: Node): ScalarShape = {
    new GraphQLNestedEnumParser(node).parse()
  }

  def parseInputObjectTypeExtension(node: Node): NodeShape = {
    GraphQLInputTypeParser(node).parse()
  }

}
