package org.yaml.lexer

import org.mulesoft.lexer.LexerInput.EofChar

/**
  * An Object to contain Character rules for The YamlLexer
  */
object YamlCharRules {

  /** Is a Char Printable ?
    * [1]	c-printable	::=	  #x9 | #xA | #xD | [#x20-#x7E]          8 bit
    *                     |  #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD]          16 bit
    *                     | [#x10000-#x 10 FF FF]                               32 bit
    */
  def isCPrintable(c: Int): Boolean =
    c == '\t' || c == '\n' || c == '\r' || c >= 0x20 && c <= 0x7E ||
      c == 0x85 || c >= 0xA0 && c <= 0xD7FF || c >= 0xE000 && c <= 0xFFFD ||
      c >= 0x10000 && c <= 0x10FFFF

  /**
    * Is a Non Blank Json character ?
    * [2]	nb-json	::=	#x9 | [#x20-#x 10 FF FF]
    */
  def isNbJson(c: Int): Boolean = c == '\t' || c >= 0x20 && c <= 0x10FFFF

  /**
    * [3]	c-byte-order-mark	::=	#xFEFF
    */
  final val BomMark = 0xFEFF

  /*
   * Is an Indicator ?
   * [22]	c-indicator	::=	“-” | “?” | “:” | “,” | “[” | “]” | “{” | “}”
   *                    | “#” | “&” | “*” | “!” | “|” | “>” | “'” | “"”
   *                    | “%” | “@” | “`”
   */
  def isIndicator(c: Int): Boolean = "-?:,[]{}#&*!>'\"%@ `".indexOf(c) != -1

  /**
    * [23]	c-flow-indicator	::=	“,” | “[” | “]” | “{” | “}”
    */
  def isFlowIndicator(chr: Int): Boolean = chr == '[' || chr == ']' || chr == '{' || chr == '}' || chr == ','

  /**
    * Is the Break Line Char
    * [24] line-feed            ::= #xA
    * [25] b-carriage-return	::=	#xD
    * [26] b-char	            ::=	b-line-feed | b-carriage-return
    */
  final def isBBreak(c: Int): Boolean = c == '\n' || c == '\r'
  final def isBreakOrEof(c:Int): Boolean = c == '\n' || c == '\r' || c == EofChar

  /**
    * Is A Not Break Char
    * [27]	nb-char	::=	c-printable - b-char - c-byte-order-mark
    */
  def isNBChar(c: Int): Boolean = isCPrintable(c) && !isBBreak(c) && c != BomMark

  /**
    * Is a whitespace Char
    * [31]	s-space	::=	#x20
    * [32]	s-tab	::=	#x9
    * [33]	s-white ::=	s-space | s-tab
    */
  def isWhite(c: Int): Boolean = c == ' ' || c == '\t'

  /**
    * Is a Non Space Char
    * [34]	ns-char
    *
    * @todo Inline to optimize ?
    */
  def isNsChar(c: Int): Boolean = isNBChar(c) && !isWhite(c)

  /**
    * Is a Decimal Digit<p>
    * [35]	nsDecDigit ::=	[#x30-#x39] (0-9)
    */
  @inline def isNsDecDigit(c: Int): Boolean = c >= '0' && c <= '9'

    /**A hexadecimal digit for escape sequences:<p>
      * [36]	ns-hex-digit	::=	  [[isNsDecDigit ns-dec-digit]] | [#x41-#x46] (A-F) | [#x61-#x66] (a-f)
      */
  @inline def isNsHexDigit(c: Int): Boolean = isNsDecDigit(c) || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f'

    /**
      * ASCII letter (alphabetic) characters:<p>
      * [37]	ns-ascii-letter	::=	[#x41-#x5A] ( A-Z )/ | [#x61-#x7A] (a-z)
      */
   @inline def isAsciiLetter(c:Int):Boolean = c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'
    /**
    * Word (alphanumeric) characters for identifiers:<p>
    * [38]	ns-word-char	::=	[[isNsDecDigit ns-dec-digit]] | [[isAsciiLetter ns-ascii-letter]] | “-”
      */
  def isWordChar(c:Int):Boolean = isNsDecDigit(c) || isAsciiLetter(c) || c == '-'


  /**
    * Is the Break Comment Char
    * [76]	b-comment	::=	b-non-content | /* End of file */
    * [30]	b-non-content	::=	b-break
    */
  def isBreakComment(c: Int): Boolean = isBreakOrEof(c)

  /** If c1 followed by c2 a mapping indicator */
  def isMappingIndicator(c1:Int, c2: =>Int): Boolean = c1 == ':' && (c2 == ' ' || isBreakComment(c2))
  /**
    * YAML Escape Sequences are a superset of C’s escape sequences:
    * [42]-[61]
    * Returns:
    * <ul>
    *   <li>the processed escaped sequence if single char</li>
    *   <li>'x', 'u', or 'U' for Unicode hexadecimal sequences</li>
    *   <li>-1 when it is not a valid escape char</li>
    * </ul>
    */
  def escapeSeq(c: Int): Int = c match {
    case '0'  => 0x00 // [42] ASCII null
    case 'a'  => 0x07 // [43] ASCII bell
    case 'b'  => 0x0b // [44] ASCII backspace
    case 't'  => 0x09 // [45] ASCII horizontal tab
    case 'n'  => 0x0A // [46] ASCII line feed
    case 'v'  => 0x0B // [47] ASCII vertical tab
    case 'f'  => 0x0C // [48] ASCII form feed
    case 'r'  => 0x0D // [49] ASCII carriage return
    case 'e'  => 0x1B // [50] ASCII escape
    case ' '  => 0x20 // [51] ASCII space
    case '"'  => 0x22 // [52] ASCII double quote
    case '/'  => 0x2F // [53] ASCII slash, for JSON compatibility.
    case '\\' => 0x5C // [54] ASCII back slash
    case 'N'  => 0x85 // [55] Unicode next line
    case '_'  => 0xA0 // [56] Unicode non-breaking space
    case 'L'  => 0x2028 // [57] Unicode line separator
    case 'P'  => 0x2029 // [58] Unicode paragraph separator
    case 'x'  => 'x' // [59] 8-bit Unicode character
    case 'u'  => 'u' // [60] 16-bit Unicode character
    case 'U'  => 'U' // [60] 32-bit Unicode character
    case _    => -1
  }
}
