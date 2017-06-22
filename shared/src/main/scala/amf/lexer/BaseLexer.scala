package amf.lexer

import amf.lexer.CharStream.EOF_CHAR

/**
  * A simple lexer for traditional languages
  */
abstract class BaseLexer[T <: Token](stream: CharStream = new CharSequenceStream()) extends AbstractLexer[T](stream) {

  // The basic grammar tokens
  protected val stringLiteralToken: T
  protected val intToken: T
  protected val doubleToken: T
  protected val decimalToken: T

  protected def findKeyword(text: String): T
  protected def findOperator(chr: Int): T

  override protected def isWhiteSpace(chr: Int): Boolean = chr.toChar.isWhitespace

  private def operator(chr: Int): T = {
    consume()
    findOperator(chr) match {
      case null => badCharToken
      case t    => t
    }
  }

  private def multiLineString(quoteChar: Int): T = ???

  private def string(chr: Int): T = {
    matches(chr)
    while (!matchAny(chr, '\n', EOF_CHAR)) {
      if (currentChar == '\\') {
        val c = lookAhead(1)
        if (c != EOF_CHAR && c != '\n') consume()
      }
      consume()
    }
    stringLiteralToken
  }

  private def blockComment(): T = {
    while (currentChar != CharStream.EOF_CHAR && !stream.matches('*', '/')) consume()
    commentToken
  }

  private def hexadecimalNumber(): T = ???

  private def number(): T = {

    matchDecimalDigits()
    var t: T = intToken
    if (matches('.')) {
      matchDecimalDigits()
      t = decimalToken
      if (t == null) t = doubleToken
    }

    //todo implement...

    if (t != null) t else intToken
  }

  private def isJavaIdentifierStart(chr: Int): Boolean = chr.toChar match {
    case c if 'a' until 'z' contains c => true
    case c if 'A' until 'Z' contains c => true
    case '_'                           => true
    case _                             => false
  }

  private def isJavaIdentifierPart(chr: Int): Boolean = isJavaIdentifierStart(chr) || Character.isDigit(chr)

  private def identifier(): T = {
    while (isJavaIdentifierPart(currentChar)) {
      consume()
    }
    findKeyword(currentTokenText.toString)
  }

  override protected val states: Array[Int => T] = Array({
    case ' ' | '\t' | '\n' | '\r' => whiteSpace()
    case chr @ ('+' | '-' | '*' | ':' | ';' | '.' | ',' | '{' | '}' | '[' | ']' | '(' | ')' | '?' | '=' | '>' | '<' |
        '$' | '&' | '|' | '!') =>
      operator(chr)
    case chr @ '"' => strings(chr)
    case '/' =>
      val a = lookAhead(1)
      if (a == '*' || a == '/') {
        consume()
        consume()
      }
      if (a == '*') blockComment() else if (a == '/') lineComment() else operator('/')
    case '0' =>
      val c = lookAhead(1)
      if (c == 'x' || c == 'X') hexadecimalNumber() else number()
    case chr =>
      if (isJavaIdentifierStart(chr)) {
        identifier()
      } else if (Character.isDigit(chr))
        number()
      else if (isWhiteSpace(chr))
        whiteSpace()
      else
        badChar()
  })

  private def strings(chr: Int) = {
    val c1 = lookAhead(1)
    val c2 = lookAhead(2)
    if (c1 == '"' && c2 == '"') multiLineString(chr) else string(chr)
  }
}
