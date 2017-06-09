package amf.json

import amf.common.AMFToken
import amf.lexer.Token
import amf.lexer.Token._

/**
  * JsonToken
  */
trait JsonToken extends Token

object JsonToken {

    object WhiteSpace extends WhiteSpace with JsonToken

    object Identifier extends Identifier with JsonToken

    object Eof extends Eof with JsonToken

    object BadChar extends BadChar with JsonToken

    object Comment extends Comment with JsonToken

    object Number extends AMFToken.Number with JsonToken

    object StringToken extends AMFToken.StringToken with JsonToken

    object Colon extends Operator(":") with JsonToken
    object Comma extends Operator(",") with JsonToken

    object StartSequence extends Operator("[") with JsonToken
    object EndSequence extends Operator("]") with JsonToken
    object StartMap extends Operator("{") with JsonToken
    object EndMap extends Operator("}") with JsonToken

    object Link extends AMFToken.Link with JsonToken

    object Root extends AMFToken.Root with JsonToken

    object True extends NamedToken("True") with JsonToken
    object False extends NamedToken("False") with JsonToken
    object Null extends NamedToken("Null") with JsonToken

    object Consume extends Consume with JsonToken

    // Logical nodes

    object MapToken extends AMFToken.MapToken with JsonToken
    object Entry extends AMFToken.Entry with JsonToken
    object SequenceToken extends AMFToken.SequenceToken with JsonToken

}
