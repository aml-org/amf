package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.AmfArray
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.internal.parser.domain.Annotations.{inferred, virtual}
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.antlrast.ast.Node

trait GraphQLCommonTypeParser extends GraphQLASTParserHelper {

  val obj: NodeShape

  protected def collectFieldsFromPath(objTypeNode: Node, fieldsAstPath: Seq[String])(implicit
      ctx: GraphQLBaseWebApiContext
  ): Unit = {
    collectNodes(objTypeNode, fieldsAstPath).foreach { fieldNode =>
      GraphQLFieldParser(fieldNode, obj).parse {
        case Left(propertyShape: PropertyShape) =>
          val properties = obj.properties ++ Seq(propertyShape)
          obj.set(NodeShapeModel.Properties, AmfArray(properties, virtual()), inferred())
        case Right(shapeOperation: ShapeOperation) =>
          val operations = obj.operations ++ Seq(shapeOperation)
          obj.set(NodeShapeModel.Operations, AmfArray(operations, virtual()), inferred())
      }
    }
  }
}
