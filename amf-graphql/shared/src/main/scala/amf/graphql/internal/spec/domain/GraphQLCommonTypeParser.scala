package amf.graphql.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import amf.shapes.internal.domain.metamodel.NodeShapeModel
import org.mulesoft.antlrast.ast.Node
import amf.graphql.internal.spec.document._

trait GraphQLCommonTypeParser extends GraphQLASTParserHelper {

  val obj: NodeShape

  protected def collectFieldsFromPath(objTypeNode: Node, fieldsAstPath: Seq[String])(implicit
      ctx: GraphQLBaseWebApiContext
  ): Unit = {
    collectNodes(objTypeNode, fieldsAstPath).foreach { fieldNode =>
      GraphQLFieldParser(fieldNode, obj).parse {
        case Left(propertyShape: PropertyShape) =>
          val properties = obj.properties :+ propertyShape
          obj set properties as NodeShapeModel.Properties

        case Right(shapeOperation: ShapeOperation) =>
          val operations = obj.operations :+ shapeOperation
          obj set operations as NodeShapeModel.Operations
      }
    }
  }
}
