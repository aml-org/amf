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

  def parse(id: String): Shape = {
    invokeAppropriateParser(id)
      .map { a: AnyShape =>
        withHashId(a, parentId = id, hashCode = typeExtensionDef.hashCode())
          .withIsExtension(true)
      }
      .getOrElse {
        AnyShape().adopted(id)
      }
  }

  /** We can declare 0+ extensions. We need to create a unique ID for each of these. Since every extension has the same
    * name (even the original non-extension shape) adopting these will produce the same ID, unless we include a unique
    * hash.
    */
  private def withHashId(a: AnyShape, parentId: String, hashCode: Int): AnyShape = {
    val originalName = a.name.value()
    val hash         = typeExtensionDef.hashCode()
    val newName      = s"$originalName-extension-$hash"

    // we create a new name just for adoption
    a
      .withName(newName)
      .adopted(parentId)
      .withName(originalName)
  }

  private def invokeAppropriateParser(id: String): Option[AnyShape] = {
    this
      .pathToNonTerminal(typeExtensionDef, Seq(SCALAR_TYPE_EXTENSION))
      .map(parseScalarTypeExtension(_, id))
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(OBJECT_TYPE_EXTENSION))
          .map(parseObjectTypeExtension(_, id))
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(INTERFACE_TYPE_EXTENSION))
          .map(parseInterfaceTypeExtension(_, id))
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(UNION_TYPE_EXTENSION))
          .map(parseUnionTypeExtension(_, id))
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(ENUM_TYPE_EXTENSION))
          .map(parseEnumTypeExtension(_, id))
      }
      .orElse {
        this
          .pathToNonTerminal(typeExtensionDef, Seq(INPUT_OBJECT_TYPE_EXTENSION))
          .map(parseInputObjectTypeExtension(_, id))
      }
  }

  def parseScalarTypeExtension(node: Node, id: String): ScalarShape = {
    new GraphQLCustomScalarParser(node).parse(id)
  }

  def parseObjectTypeExtension(node: Node, id: String): NodeShape = {
    new GraphQLNestedTypeParser(node).parse(id)
  }

  def parseInterfaceTypeExtension(node: Node, id: String): NodeShape = {
    new GraphQLNestedTypeParser(node, true).parse(id)
  }

  def parseUnionTypeExtension(node: Node, id: String): UnionShape = {
    new GraphQLNestedUnionParser(node).parse(id)

  }
  def parseEnumTypeExtension(node: Node, id: String): ScalarShape = {
    new GraphQLNestedEnumParser(node).parse(id)
  }

  def parseInputObjectTypeExtension(node: Node, id: String): NodeShape = {
    GraphQLInputTypeParser(node).parse(id)
  }

}
