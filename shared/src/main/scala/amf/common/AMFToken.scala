package amf.common

import amf.json.JsonToken
import amf.lexer.Token
import amf.lexer.Token._

/**
  * Created by pedro.colunga on 5/30/17.
  */
object AMFToken {

    case class Root() extends NamedToken("Root") with JsonToken
    case class MapToken() extends NamedToken("Map") with JsonToken
    case class Entry() extends NamedToken("Entry") with JsonToken
    case class SequenceToken() extends NamedToken("Seq") with JsonToken
    case class StringToken() extends Token.StringToken with JsonToken
    abstract class Number(override val name: String = "Number") extends Token.Number(name) with JsonToken
    case class Link() extends NamedToken("Link") with JsonToken

}


