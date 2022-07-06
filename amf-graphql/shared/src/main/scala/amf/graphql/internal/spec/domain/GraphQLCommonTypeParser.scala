package amf.graphql.internal.spec.domain

import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedField
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.operations.ShapeOperation
import org.mulesoft.antlrast.ast.Node

trait GraphQLCommonTypeParser extends GraphQLASTParserHelper {

  val obj: NodeShape

  protected def collectFieldsFromPath(objTypeNode: Node, fieldsAstPath: Seq[String])(implicit
      ctx: GraphQLWebApiContext
  ): Unit = {
    collect(objTypeNode, fieldsAstPath).foreach {
      case fieldNode: Node =>
        GraphQLFieldParser(fieldNode).parse {
          case Left(propertyShape: PropertyShape) =>
            obj.withProperties(obj.properties ++ Seq(propertyShape))
          case Right(shapeOperation: ShapeOperation) =>
            obj.withOperations(obj.operations ++ Seq(shapeOperation))
        }
      case _ => // ignore
    }
    checkFieldsAreUnique
  }

  private def checkFieldsAreUnique(implicit ctx: GraphQLWebApiContext): Unit = {
    val fields = obj.properties ++ obj.operations
    fields.foreach(field => {
      val fieldName = field.name.value()
      if (fieldIsDuplicated(fieldName, fields))
        ctx.eh.violation(
          DuplicatedField,
          field,
          s"Cannot exist two or more fields with name '$fieldName'",
          field.annotations
        )
    })
  }

  private def fieldIsDuplicated(fieldName: String, fields: Seq[NamedDomainElement]) =
    fields.count(_.name.value() == fieldName) > 1

}
