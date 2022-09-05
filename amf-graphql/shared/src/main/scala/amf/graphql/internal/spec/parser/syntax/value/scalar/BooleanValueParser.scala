package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{BOOLEAN, BOOLEAN_VALUE}
import org.mulesoft.antlrast.ast.Node

object BooleanValueParser extends AbstractScalarValueParser(Nil, BOOLEAN) {
  override def parse(n: Node, basePath: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ScalarNode] = {
    super
      .parsePath(n, basePath ++ Seq(BOOLEAN_VALUE, "'true'"))
      .orElse(super.parsePath(n, basePath ++ Seq(BOOLEAN_VALUE, "'false'")))
  }
}
