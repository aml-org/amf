package amf.parser

import amf.common.{AMFAST, AMFASTLink}
import amf.lexer.Token
import amf.parser.ASTNodePrinter.print
import amf.remote.{Context, Hint, Platform}
import amf.visitor.ASTNodeVisitor

import scala.concurrent.Future

/**
  * AST Node
  */
trait ASTNode[T <: Token] {
  type N <: ASTNode[T]

  /** Return node range on input (start and end position). */
  val range: Range

  /** Returns node annotations. */
  val annotations: Seq[Annotation]

  /** Returns node children. */
  def children: Seq[N]

  /** Returns all children of specified token. */
  def children(t: T): Seq[N]

  /** Returns an optional child in the specified position or the empty node. */
  def child(n: Int): N

  /** Return first child. */
  final def head: N = children.head

  /** Return last child. */
  final def last: N = children.last

  /** Returns node content (if defined). */
  def content: String

  /** Returns the type of the Node. */
  def `type`: T

  /** Empty node. */
  val empty: N

  /** Accept given node visitor. */
  def accept(visitor: ASTNodeVisitor): Unit = {
    visitor.before(this)
    visitor.visit(this)
    visitor.after(this)
  }

  override def toString: String = print(this)
}

trait ASTLinkNode[T <: Token] extends ASTNode[T] {

  /** Return node target. */
  def target: Document
  def resolve(remote: Platform, context: Context, hint: Option[Hint]): Future[AMFAST]

}

/** Base ASTNode. */
abstract class BaseASTNode[T <: Token](val `type`: T, val content: String, val range: Range) extends ASTNode[T] {

  /** Returns node children. */
  override def children: Seq[N]

  /** Return all children of this node with the specified Token Type. */
  override def children(t: T): Seq[N] = children.filter(_.`type` equals t)

  /** Returns optional child in specified position or empty node. */
  override def child(n: Int): N = if (n < 0 || n >= children.length) empty else children(n)
}
