package org.yaml.lexer

import java.io.{File, FileReader}

import org.mulesoft.lexer.LexerInput.EofChar
import org.mulesoft.lexer.{BaseLexer, CharSequenceLexerInput, LexerInput, LexerState}
import org.yaml.lexer.YamlLexer._
import org.yaml.lexer.YamlToken._

/**
  * Yaml Lexer for 1.2 Specification
  */
class YamlLexer(input: LexerInput = new CharSequenceLexerInput())
    extends BaseLexer[YamlToken](input) {

  var inDocument = false

  /**
    * Process all pending tokens. Trivial implementation just emit the EofToken
    * More complex ones can continue returning pending tokens until they emit the EofToken
    */
  override protected def processPending(): Unit = {
    if (inDocument) emit(EndDocument)
    emit(EndStream)
  }

  /**
    * Return the Map of function of processing for each state
    */
  override protected def stateMap: Map[LexerState, (Int) => Boolean] =
    Map(InitialState -> initial, InDirectives -> inDirectives)

  /** Get a Token at the Initial State */
  private def initial(currentChar: Int): Boolean = {
    currentChar match {
      case BomMark     => emit(Bom)
      case ' ' | '\t'  => whiteSpace()
      case '#'         => comment()
      case '\n' | '\r' => lineBreak()
      case '%'         => directive()
      case '-'         => directivesEnd()
      case _           => false
    }
  }

  /** Get a Token at the InDocument state */
  private def inDirectives(currentChar: Int): Boolean = {
    currentChar match {
      case ' ' | '\t'  => whiteSpace()
      case '#'         => comment()
      case '\n' | '\r' => lineBreak()
      case '%'         => directive()
      case '-'         => directivesEnd()
      case _           => false
    }
  }

  private def whiteSpace(): Boolean = {
    if (!isWhite(currentChar)) false
    else {
      do consume() while (isWhite(currentChar))
      emit(WhiteSpace)
    }
  }

  private def comment(): Boolean = {
    if (currentChar != '#') return false
    emit(BeginComment)
    consume()
    emit(Indicator)
    while (!isBComment(currentChar)) consume()
    emit(MetaText)
    emit(EndComment)
  }

  /**
    * [82] - [85]
    */
  private def directive(): Boolean = {
    if (currentChar != '%' || !isNsChar(lookAhead(1))) return false
    if (state != InDirectives) {
      state = InDirectives
      inDocument = true
      emit(BeginDocument)
    }
    emit(BeginDirective)
    consume()
    emit(Indicator)
    metaText()
    while (whiteSpace()) {
      metaText()
    }
    emit(EndDirective)
  }
  private def directivesEnd() =
    if (currentChar != '-' || lookAhead(1) != '-' || lookAhead(2) != '-') false
    else {
      consume(3)
      emit(DirectivesEnd)
      state = InitialState
      if (currentChar == EofChar) emitEmptyScalar()
      true
    }

  private def emitEmptyScalar(): Unit = {
    emit(BeginNode)
    emit(BeginScalar)
    emit(EndScalar)
    emit(EndNode)
  }

  private def metaText() = {
    while (isNsChar(currentChar)) consume()
    emit(MetaText)
  }

  private def lineBreak(): Boolean =
    if (currentChar == '\n') {
      consume()
      emit(LineBreak)
    } else if (currentChar == '\r') {
      consume()
      if (currentChar == '\n') consume()
      emit(LineBreak)
    } else false

}

object YamlLexer {
  private val InitialState = LexerState.Initial
  private val InDirectives = LexerState("InDirectives", 0)

  /** Is a Char Printable ?
    * [1]	c-printable	::=	  #x9 | #xA | #xD | [#x20-#x7E]          8 bit
    * |  #x85 | [#xA0-#xD7FF] | [#xE000-#xFFFD]          16 bit
    * | [#x10000-#x 10 FF FF]                               32 bit
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
  val BomMark = 0xFEFF

  /**
    * Is the Break Line Char
    * [26]    b-char
    * [28]	b-break
    */
  def isBBreak(c: Int): Boolean = c == '\n' || c == '\r'

  /**
    * Is A Not Break Char
    * [27]	nb-char	::=	c-printable - b-char - c-byte-order-mark
    */
  def isNBChar(c: Int): Boolean = isCPrintable(c) && !isBBreak(c) && c != BomMark

  /**
    * Is a whitespace Char
    * [33]	s-white
    */
  def isWhite(c: Int): Boolean = c == ' ' || c == '\t'

  /**
    * Is a Non Space Char
    * [34]	ns-char
    * @todo Inline to optimize ?
    */
  def isNsChar(c: Int): Boolean = isNBChar(c) && !isWhite(c)

  /**
    * Is the Break Comment Char
    * [76]	b-comment	::=	b-non-content | /* End of file */
    * [30]	b-non-content	::=	b-break
    */
  def isBComment(c: Int): Boolean = isBBreak(c) || c == EofChar

  def apply(input: LexerInput = new CharSequenceLexerInput()): YamlLexer = new YamlLexer(input)

  def apply(s: String): YamlLexer = YamlLexer(new CharSequenceLexerInput(s))

  def apply(file: File): YamlLexer = {
    val fis    = new FileReader(file)
    val data   = new Array[Char](file.length.toInt)
    val length = fis.read(data)
    fis.close()
    new YamlLexer(new CharSequenceLexerInput(data, 0, length, file.getName))
  }
}
