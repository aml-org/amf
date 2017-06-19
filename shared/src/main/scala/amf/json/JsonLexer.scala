package amf.json

import amf.common.AMFToken
import amf.common.AMFToken._
import amf.lexer.{BaseLexer, CharSequenceStream, CharStream}

/**
  * Created by pedro.colunga on 5/29/17.
  */
class JsonLexer(stream: CharStream = new CharSequenceStream()) extends BaseLexer[AMFToken](stream) {

  override protected val eofToken: AMFToken           = Eof
  override protected val whiteSpaceToken: AMFToken    = WhiteSpace
  override protected val commentToken: AMFToken       = Comment
  override protected val badCharToken: AMFToken       = BadChar
  override protected val stringLiteralToken: AMFToken = StringToken
  override protected val intToken: AMFToken           = IntToken
  override protected val doubleToken: AMFToken        = FloatToken
  override protected val decimalToken: AMFToken       = FloatToken

  private val keywords: Map[String, AMFToken] = Map(
    ("true", True),
    ("false", False),
    ("null", Null)
  )

  private val operators: Map[Char, AMFToken] = Map(
    (':', Colon),
    (',', Comma),
    ('{', StartMap),
    ('}', EndMap),
    ('[', StartSequence),
    (']', EndSequence)
  )

  override protected def findKeyword(text: String): AMFToken = keywords(text)

  override protected def findOperator(chr: Int): AMFToken = operators(chr.toChar)

  override protected def strings(chr: Int): AMFToken = {
    if (lookAhead(1) == '$') {
      consume(6)
      Link
    } else {
      super.strings(chr)
    }
  }
}

object JsonLexer {
  def apply(input: String) = new JsonLexer(new CharSequenceStream(input))

  def apply(stream: CharStream) = new JsonLexer(stream)
}
