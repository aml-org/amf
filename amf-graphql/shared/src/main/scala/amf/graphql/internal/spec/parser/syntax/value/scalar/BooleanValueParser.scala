package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{BOOLEAN_VALUE, FALSE, TRUE}
import org.mulesoft.antlrast.ast.Node

object BooleanValueParser extends AbstractScalarValueParser(Nil, DataType.Boolean) {
  override def parse(n: Node, basePath: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ScalarNode] = {
    val maybeTrue = super.parsePath(n, basePath ++ Seq(BOOLEAN_VALUE, TRUE))
    val maybeFalse = super.parsePath(n, basePath ++ Seq(BOOLEAN_VALUE, FALSE))
    maybeTrue orElse maybeFalse
  }
}
