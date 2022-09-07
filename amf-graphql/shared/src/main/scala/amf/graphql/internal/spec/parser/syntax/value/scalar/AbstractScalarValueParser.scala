package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.domain.ScalarNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes.toDataType
import amf.graphql.internal.spec.parser.syntax.value.AbstractValueParser
import org.mulesoft.antlrast.ast.{Node, Terminal}

abstract class AbstractScalarValueParser(
    pathToValue: Seq[String],
    typeName: String
) extends AbstractValueParser[ScalarNode]
    with GraphQLASTParserHelper {

  override def parse(n: Node, basePath: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ScalarNode] =
    parsePath(n, basePath ++ pathToValue)

  protected def parsePath(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, valuePath).map { case valueNode: Terminal =>
      val value  = trimQuotes(valueNode.value)
      val result = ScalarNode(value, toDataType.get(typeName), toAnnotations(valueNode))
      result
    }
}
