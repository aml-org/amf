package amf.lexer

trait Token

object Token {
    /** Special one. Not returned. Used to continue consuming the input */
    case class Consume()

    abstract class NamedToken(val name: String) extends Token {
        override def toString: String = name
    }
    abstract class KeyWord(override val name: String) extends NamedToken(name)
    case class Operator(override val name: String) extends NamedToken(name)
    abstract class Number(override val name: String = "Number") extends NamedToken(name)
    case class Eof() extends NamedToken("Eof")
    case class WhiteSpace() extends NamedToken("WS")
    case class Identifier() extends NamedToken("Id")
    abstract class StringToken() extends NamedToken("Str")
    abstract class BooleanToken() extends NamedToken("Bool")
    case class BadChar() extends NamedToken("BadChar")
    case class Comment() extends NamedToken("Comment")
}


