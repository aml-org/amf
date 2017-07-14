package amf.visitor

import amf.parser.{ASTLinkNode, ASTNode}

/**
  *
  */
trait ASTNodeVisitor {
  def before(node: ASTNode[_]): Unit

  def visit(node: ASTNode[_]): Unit

  def visit(node: ASTLinkNode[_]): Unit

  def after(node: ASTNode[_]): Unit
}
