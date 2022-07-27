package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.{PropertyShape, PropertyShapePath}
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.graphqlfederation.internal.spec.context.linking.LinkEvaluation
import amf.graphqlfederation.internal.spec.context.linking.fieldset.PropertyShapePathExpression
import amf.shapes.client.scala.model.domain.NodeShape
import org.mulesoft.antlrast.ast.Node

case class RequiresParser(ast: Node, target: PropertyShape, parent: NodeShape)(implicit
    val ctx: GraphQLFederationWebApiContext
) extends GraphQLASTParserHelper {
  def parse(): Unit = {
    collectNodes(ast, Seq(FIELD_DIRECTIVE, FIELD_FEDERATION_DIRECTIVE, REQUIRES_DIRECTIVE, FIELD_SET)).foreach { fieldSetAst =>
      val paths = FieldSetParser(parent, fieldSetAst).collect()
      val action =
        LinkEvaluation[Seq, PropertyShapePathExpression, PropertyShapePath, GraphQLFederationWebApiContext](paths) {
          resolvedPaths =>
            target.withRequires(resolvedPaths)
        }
      ctx.linkingActions + action
    }
  }
}
