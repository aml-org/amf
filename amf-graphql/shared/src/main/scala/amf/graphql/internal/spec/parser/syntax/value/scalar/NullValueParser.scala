package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.DataType
import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{NULL, NULL_VALUE}
import org.mulesoft.antlrast.ast.Node

object NullValueParser extends AbstractScalarValueParser(Nil, DataType.Nil) {
  override def parse(n: Node, basePath: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ScalarNode] = {
    super
      .parsePath(n, basePath ++ Seq(NULL_VALUE, NULL))
  }
}
