package org.mulesoft.lexer

/**
  * The State of the Lexer
  */
case class LexerState(name:String, ordinal:Int) {
    override def toString: String = s"$name($ordinal)"
}
object LexerState {
    final val Initial = LexerState("Initial", 0)
}
