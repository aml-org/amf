package org.mulesoft.lexer

/**
  * A lexer is a recognizer that draws input symbols from a character stream.
  * Specific lexer grammars are implementations of this interface this object.
  */
trait Lexer[T <: Token] {

  /** get the current token in the input stream.  */
  def token: T = tokenData.token

  /** All the token data.  */
  def tokenData: TokenData[T]

  /** Advance the lexer to the next token.  */
  def advance()

  /** Get the current Token Char Sequence.  */
  def tokenText: CharSequence

  /** Get the current Token String.  */
  def tokenString: String = tokenText.toString

  /** Ge the current state of the Lexer (0 = default state).  */
  def state: LexerState
}
