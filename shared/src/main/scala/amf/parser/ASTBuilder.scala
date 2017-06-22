package amf.parser

import amf.lexer.{Lexer, Token}

/**
  * Creates Abstract Syntax trees based on a simple interface that should be called
  * from the Parser while consuming tokens from Lexer.
  */
abstract class ASTBuilder[T <: Token, N <: ASTNode[T]](val lexer: Lexer[T]) {

  /** Build and return root node. */
  def root()(parse: () => Unit): N

  /** Adds the current input token to the sub-tree under construction. */
  def addChild(): Unit

  /** Advance the Lexer to the next position. */
  def advanceLexer(): Unit

  /** Starts the building of a new tree node. Every call to addChild will place a new child under the current sub-tree. */
  def beginNode(): Unit

  /** Discard selected token and advance. */
  def discard(): Unit

  /** Instruct the builder to drop the current node being built. */
  def dropNode(): Unit

  /**
    * Instruct the Builder to end the building of the current sub-tree. The provided token will be
    * used as the root of the constructed sub-tree
    */
  def endNode(token: T): Unit

  /** Returns true if the token source is exhausted. */
  def eof: Boolean

  /** Token 'n' positions ahead. */
  def lookAhead(n: Int): T

  /** Returns current token text. */
  def currentText: String

  /** The token type of the current token. */
  def currentToken: T
}
