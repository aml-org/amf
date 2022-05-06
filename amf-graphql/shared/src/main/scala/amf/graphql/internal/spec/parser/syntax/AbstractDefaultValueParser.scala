package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.domain.{ScalarNode, Shape}
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import org.mulesoft.antlrast.ast.{Node, Terminal}

abstract class AbstractDefaultValueParser(n: Node, pathToValue: Seq[String], typeName: String)
    extends GraphQLASTParserHelper {

  def parse(): Option[ScalarNode] = parseDefaultValue(n, pathToValue)

  def parseDefaultValue(n: Node, valuePath: Seq[String]): Option[ScalarNode] =
    path(n, Seq(DEFAULT_VALUE, VALUE) ++ valuePath).map { case defaultValue: Terminal =>
      val value  = trimQuotes(defaultValue.value)
      val result = ScalarNode(value, toDataType.get(typeName), toAnnotations(defaultValue))
      result
    }
}

case class IntValueParser(n: Node) extends AbstractDefaultValueParser(n: Node, Seq(INT_VALUE, INT_TERMINAL), INT)

case class FloatValueParser(n: Node)
    extends AbstractDefaultValueParser(n: Node, Seq(FLOAT_VALUE, FLOAT_TERMINAL), FLOAT)

case class StringValueParser(n: Node)
    extends AbstractDefaultValueParser(n: Node, Seq(STRING_VALUE, STRING_TERMINAL), STRING)

case class BooleanValueParser(n: Node) extends AbstractDefaultValueParser(n: Node, Seq(), BOOLEAN) {
  override def parse(): Option[ScalarNode] = {
    super
      .parseDefaultValue(n, Seq(BOOLEAN_VALUE, "'true'"))
      .orElse(super.parseDefaultValue(n, Seq(BOOLEAN_VALUE, "'false'")))
  }
}

case class EnumValueParser(n: Node)
    extends AbstractDefaultValueParser(n: Node, Seq(ENUM_VALUE, NAME, NAME_TERMINAL), ENUM)

object DefaultValueParser {
  def parseDefaultValue(n: Node): Option[ScalarNode] =
    IntValueParser(n)
      .parse()
      .orElse(FloatValueParser(n).parse())
      .orElse(StringValueParser(n).parse())
      .orElse(BooleanValueParser(n).parse())
      .orElse(EnumValueParser(n).parse())

  def putDefaultValue[T <: Shape](n: Node, shape: T): T = {
    val defaultValue = parseDefaultValue(n)
    if (defaultValue.isDefined) shape.withDefault(defaultValue.get) else shape
  }
}
