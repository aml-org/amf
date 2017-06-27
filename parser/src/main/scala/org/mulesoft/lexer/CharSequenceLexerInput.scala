package org.mulesoft.lexer

import java.lang.Character._
import java.lang.Integer.{MAX_VALUE => IntMax}

/**
  * A LexerInput backed by a CharSequence
  */
class CharSequenceLexerInput(val data: CharSequence = "",
                             val startOffset: Int = 0,
                             endOffsetValue: Int = IntMax,
                             override val sourceName: String = "")
    extends LexerInput {

    val endOffset: Int = Math.min(data.length(), endOffsetValue)

    private var _column = 0
    private var _line = 1
    private var _offset = startOffset
    private var nextOffset = startOffset
    private var _current =
        if (endOffset == 0) LexerInput.EofChar
        else {
            _column = -1
            _offset = -1
            _consume()
        }

    /** The index of the character relative to the beginning of the line, as a 16 bit java character. (0 based) */
    override def column: Int = _column

    /** The current Line number (1 based). */
    override def line: Int = _line

    /** The absolute offset (0..n) of the current character.  */
    override def offset: Int = _offset

    /** the triple (line, column, offset) */
    override def position: (Int, Int, Int) = (_line, _column, _offset)

    /** The current code point character in the input (or LexerInput#Eof if the EoF was reached).  */
    override def current: Int = _current

    /** Consume and advance to the next code point.  */
    override def consume(): Unit = {
        _current = _consume()
    }

    /** Consume and advance to the next code point.  */

    private def _consume(): Int = {
        if (_current == '\n') {
            _column = 0
            _line += 1
        }
        else _column += nextOffset - _offset
        _offset = nextOffset
        if (_offset >= endOffset) return LexerInput.EofChar

        val chr = data.charAt(_offset)
        nextOffset += 1

        // Check extended code points
        if (isHighSurrogate(chr) && nextOffset < endOffset) {
            val c2 = data.charAt(nextOffset)
            if (isLowSurrogate(c2)) {
                nextOffset += 1
                return toCodePoint(chr, c2)
            }
        }
        chr
    }

    /**
      * Return the character `i` characters ahead of the current position, (or LexerInput#Eof if the EoF was reached).
      */
    override def lookAhead(i: Int): Int = {
        assert(i >= 0)
        if (i == 0) return current
        val off = nextOffset + i - 1
        if (off >= endOffset) return LexerInput.EofChar
        val chr = data.charAt(off)
        if (isHighSurrogate(chr) && off+1 < endOffset && isLowSurrogate(data.charAt(off+1))) {
            toCodePoint(chr, data.charAt(off+1))
        }
        else chr
    }

    /** Return the sub sequence of characters between the specified positions */
    override def subSequence(start: Int, end: Int): CharSequence = {
        if (start < startOffset || end > endOffset || end < start) throw new IllegalArgumentException("Invalid sub-sequence")
        data.subSequence(start, end)
    }
}
