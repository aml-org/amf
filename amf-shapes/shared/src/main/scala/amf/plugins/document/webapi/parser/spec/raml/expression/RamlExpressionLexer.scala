package amf.plugins.document.webapi.parser.spec.raml.expression

import amf.core.annotations.LexicalInformation
import amf.core.parser.{Annotations, Position}
import amf.plugins.document.webapi.parser.spec.raml.expression.Token._

import scala.collection.mutable

private[expression] class RamlExpressionLexer(private val expression: String, position: Position = Position.ZERO) {

  private var lineOffset       = position.line
  private var lastLineOffset   = position.line
  private var columnOffset     = position.column
  private var lastColumnOffset = position.column

  def lex(): Seq[Token] = {
    val iterator   = expression.iterator.buffered
    var tokenQueue = mutable.Queue[Token]()
    while (iterator.hasNext) {
      val current = iterator.next()
      val token = current match {
        case '('                         => charToken(START_GROUP, "(")
        case ')'                         => charToken(END_GROUP, ")")
        case '['                         => charToken(START_ARRAY, "[")
        case ']'                         => charToken(END_ARRAY, "]")
        case '|'                         => charToken(UNION, "|")
        case other if other.isWhitespace => charToken(WHITESPACE, other.toString)
        case other                       => symbol(other, iterator)
      }
      tokenQueue += token
    }
    tokenQueue.filter(t => t.token != WHITESPACE)
  }

  private def charToken(lexeme: String, value: String) = {
    columnOffset += value.length
    val token = Token(lexeme, value, lexicalInformation())
    lastColumnOffset = columnOffset
    token
  }

  private def lexicalInformation(): LexicalInformation =
    LexicalInformation(lastLineOffset, lastColumnOffset, lineOffset, columnOffset)

  private def symbol(current: Char, chars: BufferedIterator[Char]) = {
    var acc = current.toString
    while (chars.hasNext && isSymbolChar(chars.headOption)) {
      acc += chars.next()
    }
    columnOffset += acc.length
    val token = Token(SYMBOL, acc, lexicalInformation())
    lastColumnOffset = columnOffset
    token
  }

  def isSymbolChar(char: Option[Char]): Boolean = char.exists(isSymbolChar)

  def isSymbolChar(char: Char): Boolean = !(reservedTokens.contains(char) || char.isWhitespace)

  val reservedTokens = "()[]|"
}
