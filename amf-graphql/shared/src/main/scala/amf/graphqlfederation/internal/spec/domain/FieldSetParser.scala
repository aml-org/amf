package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.Shape
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.graphqlfederation.internal.spec.context.linking.fieldset.UnresolvedPropertyShapePath
import org.mulesoft.antlrast.ast.Node

case class FieldSetParser(source: Shape, fieldSet: Node)(implicit val ctx: GraphQLFederationWebApiContext)
    extends GraphQLASTParserHelper {

  def collect(): Seq[UnresolvedPropertyShapePath] = collect(fieldSet, UnresolvedPropertyShapePath(source, Nil))

  private def collect(
      fieldSet: Node,
      previousPath: UnresolvedPropertyShapePath
  ): Seq[UnresolvedPropertyShapePath] = {
    collectNodes(fieldSet, Seq(FIELD_SET_COMPONENT)).flatMap { component =>
      val currentPath = previousPath + findName(component, "error", s"Cannot find name from fieldSet component at ${component.location.range.toString}")

      collectNodes(component, Seq(NESTED_FIELD_SET, FIELD_SET)) match {
        case Nil             => Seq(currentPath)
        case nestedFieldSets => nestedFieldSets.flatMap(collect(_, currentPath))
      }
    }
  }

}
