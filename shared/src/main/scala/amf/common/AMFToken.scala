package amf.common

import amf.lexer.Token
import amf.lexer.Token._

/**
  * Created by pedro.colunga on 5/30/17.
  */
trait AMFToken extends Token

object AMFToken {

  object WhiteSpace extends WhiteSpace with AMFToken

  object Identifier extends Identifier with AMFToken

  object StringToken extends StringToken with AMFToken

  object BooleanToken extends BooleanToken with AMFToken

  object True extends NamedToken("True") with AMFToken

  object False extends NamedToken("False") with AMFToken

  object Null extends NamedToken("Null") with AMFToken

  object Eof extends Eof with AMFToken

  object BadChar extends BadChar with AMFToken

  object Comment extends Comment with AMFToken

  object IntToken extends Number("Number") with AMFToken

  object FloatToken extends Number("Number") with AMFToken

  object StartDocument extends Operator("---") with AMFToken

  object EndDocument extends Operator("...") with AMFToken

  object StartSequence extends Operator("[") with AMFToken

  object EndSequence extends Operator("]") with AMFToken

  object StartMap extends Operator("{") with AMFToken

  object EndMap extends Operator("}") with AMFToken

  object Colon extends Operator(":") with AMFToken

  object Comma extends Operator(",") with AMFToken

  case class Directive(override val name: String) extends NamedToken(name) with AMFToken

  object Tag extends NamedToken("Tag") with AMFToken

  object Root extends NamedToken("Root") with AMFToken

  object Consume extends Consume with AMFToken

  // Logical nodes

  object Link extends NamedToken("Link") with AMFToken

  object Library extends NamedToken("Library") with AMFToken

  object MapToken extends NamedToken("Map") with AMFToken

  object Entry extends NamedToken("Entry") with AMFToken

  object SequenceToken extends NamedToken("Seq") with AMFToken
}
