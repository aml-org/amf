package amf.visitor

import amf.parser.{ASTLinkNode, ASTNode}

/**
  * Created by pedro.colunga on 5/29/17.
  */
trait ASTNodeVisitor {
  def before(node: ASTNode[_]): Unit

  def visit(node: ASTNode[_]): Unit

  def visit(node: ASTLinkNode[_]): Unit

  def after(node: ASTNode[_]): Unit
}
