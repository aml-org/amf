package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.graphqlfederation.internal.spec.context.linking.fieldset.PropertyShapePathExpression
import org.mulesoft.antlrast.ast.Node

case class FieldSetParser(source: Shape, fieldSet: Node)(implicit val ctx: GraphQLFederationWebApiContext)
    extends GraphQLASTParserHelper {

  def collect(): Seq[PropertyShapePathExpression] = collect(fieldSet, PropertyShapePathExpression(source, Nil))

  private def collect(
      fieldSet: Node,
      previousPath: PropertyShapePathExpression
  ): Seq[PropertyShapePathExpression] = {
    collectNodes(fieldSet, Seq(FIELD_SET_COMPONENT)).flatMap { component =>
      val (propName, annotations) = findName(component, "error", s"Cannot find name from fieldSet component at ${component.location.range.toString}")
      val currentPath = previousPath + (propName, annotations)

      collectNodes(component, Seq(NESTED_FIELD_SET, FIELD_SET)) match {
        case Nil             => Seq(currentPath)
        case nestedFieldSets => nestedFieldSets.flatMap(collect(_, currentPath))
      }
    }
  }

}
