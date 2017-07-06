package org.mulesoft.lexer

/**
  * The State of the Lexer
  */
trait LexerState {
    /** Invoke when entering the state */
    def begin(): Unit = {}
    /** Invoke when exiting the state */
    def end(): Unit = {}
    /** Invoke when restoring the state */
    def restore():Unit = {}
    /** Invoke over chars in the current state */
    def apply(chr: Int): Unit

    override def toString: String = {
        val a = getClass.getName.split("\\$")
        a(a.length-1)
    }
}


