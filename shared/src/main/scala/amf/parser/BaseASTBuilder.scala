package amf.parser

import amf.lexer.Token.{Eof, WhiteSpace}
import amf.lexer.{Lexer, Token}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Base ASTBuilder
  */
abstract class BaseASTBuilder[T <: Token, N <: ASTNode[T]](lexer: Lexer[T]) extends ASTBuilder[T, N](lexer) {
  val tokens: List[N]                                = slurpTokens()
  private val stack: mutable.ArrayStack[NodeBuilder] = mutable.ArrayStack()
  private var index: Int                             = -1
  private val last                                   = tokens.size - 1

  private def slurpTokens(): List[N] = {
    val buf: ListBuffer[N] = ListBuffer()
    while (true) {
      val token = lexer.currentToken
      if (accepts(token)) {
        buf += createNode(token, lexer.currentTokenText.toString, lexer.currentRange)
      }
      token match {
        case Eof() => return buf.toList
        case _     => lexer.advance()
      }
    }
    buf.toList
  }

  advanceLexer()

  /** True if node should be created for given token. */
  protected def accepts(token: T): Boolean = true

  protected def createNode(token: T, content: String, range: Range): N

  protected def createNode(token: T, range: Range, children: Seq[N]): N

  protected def buildAST(token: T): N = {
    val n = stack.pop
    createNode(token, n.range, n.nodes)
  }

  /** Adds the current input token to the sub-tree under construction. */
  override def addChild(): Unit = addChild(current)

  /** Advance the Lexer to the next position. */
  override def advanceLexer(): Unit = {
    while (index < last) {
      index += 1
      currentToken match {
        case WhiteSpace() => // Skip
        case _            => return
      }
    }
  }

  /** Starts the building of a new tree node. Every call to addChild will place a new child under the current sub-tree. */
  override def beginNode(): Unit = {
    stack.push(NodeBuilder(current.range))
  }

  private def current = tokens(index)

  /** Discard selected token and advance. */
  override def discard(): Unit = advanceLexer()

  /** Instruct the builder to drop the current node being built. */
  override def dropNode(): Unit = {
    val drop = stack.pop
    stack.head.nodes ++= drop.nodes
  }

  /**
    * Instruct the Builder to end the building of the current sub-tree. The provided token will be
    * used as the root of the constructed sub-tree
    */
  override def endNode(token: T): Unit = addChild(buildAST(token))

  /** Returns true if the token source is exhausted. */
  override def eof: Boolean = index >= last

  /** A token 'n' positions ahead. */
  override def lookAhead(offset: Int): T = tokens(index + offset).`type`

  /** Returns current token text. */
  override def currentText: String = current.content

  /** The token type of the current token. */
  override def currentToken: T = current.`type`

  private def addChild(n: N) = stack.head.add(n)

  case class NodeBuilder(range: Range, nodes: ListBuffer[N] = ListBuffer()) {
    def add(n: N): Unit = nodes += n

    override def toString: String = nodes.mkString(", ")
  }
}
