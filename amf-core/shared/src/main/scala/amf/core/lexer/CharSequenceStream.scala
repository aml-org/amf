package amf.core.lexer

import amf.core.parser.Position

/**
  * A CharStream backed by a CharSequence
  */
class CharSequenceStream(val data: CharSequence, val startOffset: Int, val endOffset: Int, val sourceName: String = "")
    extends CharStream {

  /** Create an instance based on a CharSequence. */
  def this(sourceName: String, data: CharSequence) = this(data, 0, data.length(), sourceName)

  /** Create an instance based on a CharSequence. */
  def this(data: CharSequence) = this("", data)

  /** Create an instance based on the empty String. */
  def this() = this(data = "")

  /** The index of the character relative to the beginning of the line 0.  */
  override def column: Int = column_

  private var column_ : Int = 0

  /** Line number [1..n] within the input.  */
  private var line = 1

  /** The absolute index [0..n] of the current character.  */
  private var pointer: Int = startOffset

  override def length: Int = endOffset - startOffset

  override def subSequence(start: Int, end: Int): CharSequence = {
    if (!(start >= startOffset && end <= endOffset && start <= end))
      throw new IllegalArgumentException(s"Invalid sub-sequence start: $start, end: $end")
    data.subSequence(start, end)
  }

  override def charAt(index: Int): Char = data.charAt(index)

  /** Consume and advance to the next character.  */
  override def consume(): Unit = {
    if (pointer < endOffset) {
      column_ += 1
      val c = data.charAt(pointer)
      if (c == '\n') {
        line += 1
        column_ = 0
      }
      pointer += 1
    }
  }

  /** The current character in the input stream.  */
  override def currentChar(): Int = lookAhead(0)

  /** The absolute index [0..n] of the current character.  */
  override def index(): Int = pointer

  /** Position of the current character.  */
  override def position(): Position = Position(line, column_)

  /** Return the character `i` characters ahead of the current position, or CharStream.EOF_CHAR if the EOF is reached. */
  override def lookAhead(i: Int): Int =
    if (i >= 0 && pointer + i < endOffset) data.charAt(pointer + i) else CharStream.EOF_CHAR

  override def toString: String = data.toString
}
