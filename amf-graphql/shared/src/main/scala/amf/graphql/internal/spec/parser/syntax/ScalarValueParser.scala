package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.domain.{ScalarNode, Shape}
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.operations.ShapeParameter
import org.mulesoft.antlrast.ast.{Node, Terminal}

abstract class ScalarValueParser(
    n: Node,
    pathToValue: Seq[String],
    typeName: String
) extends GraphQLASTParserHelper {

  def parse(): Option[ScalarNode]        = parseValue(n, pathToValue)
  def parseDefault(): Option[ScalarNode] = parseDefaultValue(n, pathToValue)

  def parseValue(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, Seq(VALUE) ++ valuePath).map { case valueNode: Terminal =>
      val value  = trimQuotes(valueNode.value)
      val result = ScalarNode(value, toDataType.get(typeName), toAnnotations(valueNode))
      result
    }

  def parseDefaultValue(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, Seq(DEFAULT_VALUE, VALUE) ++ valuePath).map { case defaultValue: Terminal =>
      val value  = trimQuotes(defaultValue.value)
      val result = ScalarNode(value, toDataType.get(typeName), toAnnotations(defaultValue))
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

  override def parseDefault(): Option[ScalarNode] = {
    super
      .parseDefaultValue(n, Seq(BOOLEAN_VALUE, "'true'"))
      .orElse(super.parseDefaultValue(n, Seq(BOOLEAN_VALUE, "'false'")))
  }
}

case class EnumValueParser(n: Node) extends ScalarValueParser(n: Node, Seq(ENUM_VALUE, NAME, NAME_TERMINAL), ENUM)

object ScalarValueParser {
  def parseValue(n: Node): Option[ScalarNode] = IntValueParser(n)
    .parse()
    .orElse(FloatValueParser(n).parse())
    .orElse(StringValueParser(n).parse())
    .orElse(BooleanValueParser(n).parse())
    .orElse(EnumValueParser(n).parse())

  def parseDefaultValue(n: Node): Option[ScalarNode] = IntValueParser(n)
    .parseDefault()
    .orElse(FloatValueParser(n).parseDefault())
    .orElse(StringValueParser(n).parseDefault())
    .orElse(BooleanValueParser(n).parseDefault())
    .orElse(EnumValueParser(n).parseDefault())
}
