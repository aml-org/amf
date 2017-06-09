package amf.json

import amf.json.JsonToken._
import amf.lexer.{BaseLexer, CharSequenceStream, CharStream}

/**
  * Created by pedro.colunga on 5/29/17.
  */
class JsonLexer(stream: CharStream = new CharSequenceStream()) extends BaseLexer[JsonToken](stream){

    override protected val eofToken: JsonToken = Eof
    override protected val whiteSpaceToken: JsonToken = WhiteSpace
    override protected val commentToken: JsonToken = Comment
    override protected val badCharToken: JsonToken = BadChar
    override protected val stringLiteralToken: JsonToken = StringToken
    override protected val intToken: JsonToken = Number
    override protected val doubleToken: JsonToken = Number
    override protected val decimalToken: JsonToken = Number

    private val keywords : Map[String, JsonToken] = Map(
        ("true", True),
        ("false", False),
        ("null", Null)
    )

    private val operators : Map[Char, JsonToken] = Map(
        (':', Colon),
        (',', Comma),
        ('{', StartMap),
        ('}', EndMap),
        ('[', StartSequence),
        (']', EndSequence)
    )

    override protected def findKeyword(text: String): JsonToken = keywords(text)

    override protected def findOperator(chr: Int): JsonToken = operators(chr.toChar)

    override protected def strings(chr: Int): JsonToken = {
        if(lookAhead(1) == '$') {
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
