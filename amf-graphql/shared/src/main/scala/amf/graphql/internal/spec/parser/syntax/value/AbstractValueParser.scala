package amf.graphql.internal.spec.parser.syntax.value

import amf.core.client.scala.model.domain.DataNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import org.mulesoft.antlrast.ast.Node

trait AbstractValueParser[T <: DataNode] {
  def parse(node: Node, path: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[T]
}
