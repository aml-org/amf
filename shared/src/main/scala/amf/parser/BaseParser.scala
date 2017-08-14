package amf.parser

import amf.lexer.Token

/**
  * Base parser
  */
abstract class BaseParser[T <: Token, N <: ASTNode[T]](private val builder: ASTBuilder[T, N]) extends Parser {

  /** Get current token. */
  protected final def current: T = builder.currentToken

  /** Get current text. */
  protected final def currentText: String = builder.currentText

  /** Return true if current token matches any of the given ones. */
  protected def currentEq(tokens: T*): Boolean =
    !eof && tokens.exists(_ eq current)

  /** Token 'n' positions ahead. */
  protected def lookAhead(n: Int): T = builder.lookAhead(n)

  protected def eof: Boolean = builder.eof

  protected def consume(): Unit = {
    builder.addChild()
    builder.advanceLexer()
  }

  /** Starts the construction of a new tree. */
  def beginTree(): Unit = builder.beginNode()

  /** Discard current token. */
  def discard(): Unit = builder.discard()

  /** Discard current token if matches specified token. */
  def discard(token: T): Unit = if (currentEq(token)) discard()

  /** Discard current token if matches specified token or throw error. */
  def discardOrError(token: T): Unit = if (currentOrError(token)) discard()

  /** Finish the construction of the current tree. */
  def endTree(token: T): Unit = builder.endNode(token)

  def matchOrError(token: T): Boolean = {
    val b = currentOrError(token)
    if (b) consume()
    b
  }

  protected def currentEq(token: T): Boolean = current.eq(token)

  protected def currentNotEq(token: T): Boolean = !currentEq(token)

  protected def currentOrError(token: T): Boolean = current match {
    case `token` => true
    case _       =>
      // todo Error
      builder.error(s"expected '$token' but '$current' found")
      false
  }

  protected def parseList(listNode: T,
                          leftToken: T,
                          sepToken: T,
                          rightToken: T,
                          elementParse: () => Boolean): Boolean = {
    val started = currentEq(leftToken)
    if (started) {
      beginTree()
      discard()
      while (currentNotEq(rightToken)) {
        // add loopCheck()
        if (!elementParse()) return false
        if (currentNotEq(rightToken) && currentOrError(sepToken)) {
          discard()
        }
      }
      discardOrError(rightToken)
      endTree(listNode)
    }
    started
  }
}
