package amf.core.lexer

import amf.core.parser.Position

/**
  * A source of characters for a Lexer.
  */
abstract class CharStream extends CharSequence {

  /** The index of the character relative to the beginning of the line 0.  */
  def column: Int

  /** Consume and advance to the next character.  */
  def consume()

  /** The current character in the input stream.  */
  def currentChar(): Int

  /** The absolute index (0..n) of the current character.  */
  def index(): Int

  /** Position of the current character.  */
  def position(): Position

  /**
    * Return the character `i` characters ahead of the current position, or
    * [CharStream.EOF_CHAR] if the EOF is reached.
    */
  def lookAhead(i: Int): Int

  /**
    * Match the current character with the argument provided. If the match is successful true is
    * returned and the character is consumed. Otherwise false is returned and the stream is not
    * advanced.
    */
  def matches(c: Int): Boolean = {
    val b = c == currentChar()
    if (b) consume()
    b
  }

  /**
    * Match the current character and the next one with the argument provided. If the match is successful true is
    * returned and the character are consumed. Otherwise false is returned and the stream is not
    * advanced.
    */
  def matches(c1: Int, c2: Int): Boolean = {
    val b = c1 == currentChar() && c2 == lookAhead(1)
    if (b) {
      consume()
      consume()
    }
    b
  }

  /**
    * Similar to matches but tries to match with any of the characters provided.
    * If the match is successful true is returned and the character is consumed. Otherwise false
    * is returned and the stream is not advanced.
    */
  def matchAny(chars: Int*): Boolean = {
    val current = currentChar()
    for {
      c <- chars
    } {
      if (c == current) {
        consume()
        true
      }
    }
    false
  }

  /** The Position of the current character.  */
  //  def position(): Position

  /** Return the name of the source (Usually a file name, or similar).  */
  val sourceName: String

}

object CharStream {
  val EOF_CHAR: Int = -1
}
