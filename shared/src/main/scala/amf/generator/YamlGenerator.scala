package amf.generator

import amf.common.AMFToken._
import amf.parser.{ASTLinkNode, ASTNode}
import amf.visitor.ASTNodeVisitor

/**
  *
  */
class YamlGenerator extends ASTNodeVisitor {

  private val writer: IndentedWriter = new IndentedWriter

  /** Generate yaml for specified document. */
  def generate(root: ASTNode[_]): IndentedWriter = {
    root.accept(this)
    writer
  }

  override def before(node: ASTNode[_]): Unit = {
    node.`type` match {
      case MapToken      => writer.indent()
      case SequenceToken => writer.line().indent()
      case _             =>
    }
  }

  override def visit(node: ASTNode[_]): Unit = {
    node.`type` match {
      case Root                  => visitChildren(node.head, forceLine = false)
      case MapToken              => visitChildren(node)
      case SequenceToken         => visitChildren(node, "-", forceLine = false)
      case Entry                 => visitEntry(node)
      case IntToken | FloatToken => writer.write(' ').write(node.content)
      case StringToken           => writer.write(' ').write(node.content)
      case _                     => ???
    }
  }

  override def visit(node: ASTLinkNode[_]): Unit = {
    writer.write(" !include ").write(node.target.location().input)
  }

  override def after(node: ASTNode[_]): Unit = {
    node.`type` match {
      case MapToken | SequenceToken => writer.outdent()
      case _                        =>
    }
  }

  private def visitEntry(entry: ASTNode[_]): Unit = {
    writer.write(entry.head.content).write(":")
    entry.last.accept(this)
  }

  def visitChildren(parent: ASTNode[_], prefix: String = "", forceLine: Boolean = true): Unit = {
    var first = true
    parent.children.foreach(c => {
      if (!first || forceLine) { writer.line() }
      writer.write(prefix)
      c.accept(this)
      first = false
    })
  }
}
