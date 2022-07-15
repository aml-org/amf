package amf.graphqlfederation.internal.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShapePath
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.graphqlfederation.internal.spec.context.GraphQLFederationWebApiContext
import amf.graphqlfederation.internal.spec.context.linking.LinkAction
import amf.graphqlfederation.internal.spec.context.linking.fieldset.UnresolvedPropertyShapePath
import amf.shapes.client.scala.model.domain.NodeShape
import amf.shapes.client.scala.model.domain.federation.Key
import org.mulesoft.antlrast.ast.Node

case class KeyParser(ast: Node, target: NodeShape, basePath: Seq[String])(implicit
    val ctx: GraphQLFederationWebApiContext
) extends GraphQLASTParserHelper {
  def parse(): Unit = {
    collectNodes(ast, basePath ++ Seq(KEY_DIRECTIVE)).foreach { keyAst =>
      collectNodes(keyAst, Seq(FIELD_SET)).foreach { fieldSetAst =>
        val paths = FieldSetParser(target, fieldSetAst).collect()
        val action =
          LinkAction[Seq, UnresolvedPropertyShapePath, PropertyShapePath, GraphQLFederationWebApiContext](paths) {
            resolvedPaths =>
              val key = Key().withComponents(resolvedPaths)
              parseResolvable(keyAst, key)
              val keys = target.keys :+ key
              target.withKeys(keys)
          }
        ctx.linkingActions + action
      }
    }
  }

  private def parseResolvable(ast: Node, key: Key): Unit = {
    pathToTerminal(ast, Seq(RESOLVABLE_KEYWORD)).map { _ =>
      pathToTerminal(ast, Seq(BOOLEAN_VALUE_F, FALSE_F)).map(_ => key.withResolvable(false))
      pathToTerminal(ast, Seq(BOOLEAN_VALUE_F, TRUE_F)).map(_ => key.withResolvable(true))
    }
  }
}
