package amf.lexer

import amf.parser.Position
import amf.parser.Range

/**
  * A lexer is a recognizer that draws input symbols from a character stream.
  * Specific lexer grammars are implementations of this interface this object.
  */
trait Lexer[T <: Token] {
    /** Advance the lexer to the next token.  */
    def advance()

    /** Get the start position of the current token.  */
    def currentStart: Position

    /** Get range of current token.  */
    def currentRange: Range = Range(currentStart, currentTokenEnd - currentTokenStart)

    /** get the current token in the input stream.  */
    def currentToken: T

    /** Get the index of the end position if the current token.  */
    def currentTokenEnd: Int

    /** Get the index of the start position if the current token.  */
    def currentTokenStart: Int

    /** Get the current Token String.  */
    def currentTokenText: CharSequence

    /** Ge the current state of the Lexer (0 = default state).  */
    def state: Int

    /** Set the current state of the Lexer (0 = default state).  */
    def state_=(initialState: Int): Unit
}