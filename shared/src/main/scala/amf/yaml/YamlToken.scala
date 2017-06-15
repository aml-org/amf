package amf.yaml

import amf.common.AMFToken
import amf.lexer.Token
import amf.lexer.Token._

/**
  * YamlToken
  */
trait YamlToken extends Token

object YamlToken {

    object WhiteSpace extends WhiteSpace with YamlToken

    object Identifier extends Identifier with YamlToken


    object StringToken extends AMFToken.StringToken with YamlToken
    object BooleanToken extends AMFToken.BooleanToken with YamlToken

    object Eof extends Eof with YamlToken

    object BadChar extends BadChar with YamlToken

    object Comment extends Comment with YamlToken

    object IntToken extends AMFToken.Number with YamlToken
    object FloatToken extends AMFToken.Number with YamlToken

    object StartDocument extends Operator("---") with YamlToken

    object EndDocument extends Operator("...") with YamlToken

    object StartSequence extends Operator("[") with YamlToken

    object EndSequence extends Operator("]") with YamlToken

    object StartMap extends Operator("{") with YamlToken

    object EndMap extends Operator("}") with YamlToken

    object Comma extends Operator(",") with YamlToken

    case class Directive(override val name: String) extends NamedToken(name) with YamlToken

    object Link extends AMFToken.Link with YamlToken

    object Root extends AMFToken.Root with YamlToken

    object Consume extends Consume with YamlToken

    // Logical nodes

    object MapToken extends AMFToken.MapToken with YamlToken
    object Entry extends AMFToken.Entry with YamlToken
    object SequenceToken extends AMFToken.SequenceToken with YamlToken

}
