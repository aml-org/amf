package org.mulesoft.lexer

/**
  * A source of characters for a Lexer.
  */
trait LexerInput {

  /** The current code point character in the input (or LexerInput#Eof if the EoF was reached).  */
  def current: Int = lookAhead(0)

  /** The absolute offset (0..n) of the current character.  */
  def offset: Int

  /** The index of the character relative to the beginning of the line, as a 16 bit java character. (0 based) */
  def column: Int

  /** The current Line number (0 based). */
  def line: Int

  /** the triple (line, column, offset) */
  def position: (Int,Int, Int)

  /** Consume and advance to the next code point.  */
  def consume(): Unit

  /**
    * Return the character `i` characters ahead of the current position, (or LexerInput#Eof if the EoF was reached).
    */
  def lookAhead(i: Int): Int

  /** Return the sub-sequence of characters between the specified positions */
  def subSequence(start: Int, end: Int): CharSequence

  /** Return the name of the source, if existent (Usually a file name, or similar).  */
  val sourceName: String = ""

}

object LexerInput {
  final val EofChar: Int = -1
}
