package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.domain.{ScalarNode, Shape}
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

abstract class ScalarValueParser(
    n: Node,
    pathToValue: Seq[String],
    typeName: String
) extends GraphQLASTParserHelper {

  def parse(): Option[ScalarNode] = parseValue(n, pathToValue)

  def parseValue(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, Seq(VALUE) ++ valuePath).map { case valueNode: Terminal =>
      val value  = trimQuotes(valueNode.value)
      val result = ScalarNode(value, toDataType.get(typeName), toAnnotations(valueNode))
      result
    }
}

case class IntValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(INT_VALUE, INT_TERMINAL), INT)

case class FloatValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(FLOAT_VALUE, FLOAT_TERMINAL), FLOAT)

case class StringValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(STRING_VALUE, STRING_TERMINAL), STRING)

case class BooleanValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(), BOOLEAN) {
  override def parse(): Option[ScalarNode] = {
    super
      .parseValue(n, Seq(BOOLEAN_VALUE, "'true'"))
      .orElse(super.parseValue(n, Seq(BOOLEAN_VALUE, "'false'")))
  }
}

case class EnumValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(ENUM_VALUE, NAME, NAME_TERMINAL), ENUM)

object ScalarValueParser {
  def parseValue(n: Node): Option[ScalarNode] =
    IntValueParser(n)
      .parse()
      .orElse(FloatValueParser(n).parse())
      .orElse(StringValueParser(n).parse())
      .orElse(BooleanValueParser(n).parse())
      .orElse(EnumValueParser(n).parse())
}

object DefaultValueParser {
  def parseDefaultValue(n: Node): Option[ScalarNode] = ScalarValueParser.parseValue(n)

  def putDefaultValue[T <: Shape](n: Node, shape: T): T = {
    val defaultValue = parseDefaultValue(n)
    if (defaultValue.isDefined) shape.withDefault(defaultValue.get) else shape
  }
}
