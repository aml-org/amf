package amf.parser

import amf.lexer.Token

/**
  * AST Factory
  */
trait ASTFactory[T <: Token, N <: ASTNode[T]] {

  /** Create T node with given content. */
  def createNode(token: T, content: String, range: Range): N

  /** Create T node with given children. */
  def createNode(token: T, range: Range, children: Seq[N]): N

}
