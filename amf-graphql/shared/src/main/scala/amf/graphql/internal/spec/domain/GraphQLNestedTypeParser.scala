package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{FIELDS_DEFINITION, FIELD_DEFINITION}
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import org.mulesoft.antlrast.ast.Node

class GraphQLNestedTypeParser(objTypeNode: Node)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {

  def parse(parentId: String): NodeShape = {
    val obj  = NodeShape()
    obj.adopted(parentId)
    val name = findName(objTypeNode, "AnonymousNestedType", "", "Missing name for root nested type")
    obj.withName(name).adopted(parentId)
    collect(objTypeNode, Seq(FIELDS_DEFINITION, FIELD_DEFINITION)).foreach {
      case fieldNode: Node =>
        GraphQLFieldParser(fieldNode).parse {
          case Left(propertyShape: PropertyShape) =>
            propertyShape.adopted(obj.id)
            obj.withProperties(obj.properties ++ Seq(propertyShape))
          case Right(shapeOperation: ShapeOperation) =>
            shapeOperation.adopted(obj.id)
            obj.withOperations(obj.operations ++ Seq(shapeOperation))
        }
      case _ => // ignore
    }
    obj
  }
}
