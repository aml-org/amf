package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.domain.{AmfScalar, ScalarNode}
import amf.core.internal.metamodel.domain.ScalarNodeModel
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.value.AbstractValueParser
import org.mulesoft.antlrast.ast.{Node, Terminal}
import amf.graphql.internal.spec.document._

abstract class AbstractScalarValueParser(
    pathToValue: Seq[String],
    dataType: String
) extends AbstractValueParser[ScalarNode]
    with GraphQLASTParserHelper {

  override def parse(n: Node, basePath: Seq[String])(implicit ctx: GraphQLBaseWebApiContext): Option[ScalarNode] =
    parsePath(n, basePath ++ pathToValue)

  protected def parsePath(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, valuePath).map { case valueNode: Terminal =>
      val value  = trimQuotes(valueNode.value)
      val ann    = toAnnotations(valueNode)
      val result = ScalarNode(value, None, ann)
      result set AmfScalar(dataType, ann) as ScalarNodeModel.DataType
    }
}
