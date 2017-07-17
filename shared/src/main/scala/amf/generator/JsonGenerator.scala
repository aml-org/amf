package amf.generator

import amf.common.AMFToken._
import amf.parser.{ASTLinkNode, ASTNode}
import amf.visitor.ASTNodeVisitor

/**
  *
  */
class JsonGenerator extends ASTNodeVisitor {

  private val writer: IndentedWriter = new IndentedWriter

  /** Generate json for specified document. */
  def generate(root: ASTNode[_]): IndentedWriter = {
    root.accept(this)
    writer

  }

  override def before(node: ASTNode[_]): Unit = {
    node.`type` match {
      case MapToken      => writer.write('{').line().indent()
      case SequenceToken => writer.write('[').line().indent()
      case _             =>
    }
  }

  override def visit(node: ASTNode[_]): Unit = {
    node.`type` match {
      case Root | MapToken | SequenceToken => visitChildren(node)
      case Entry                           => visitEntry(node)
      case IntToken                        => writer.write(node.content)
      case FloatToken                      => writer.write(node.content)
      case StringToken                     => writer.quoted(node.content)
      case _                               => ???
    }
  }

  override def visit(node: ASTLinkNode[_]): Unit = {
    writer
      .write('{')
      .line()
      .indent()
      .quoted("$ref")
      .write(": ")
      .quoted(node.target.location().input)
      .outdent()
      .line()
      .write('}')
  }

  override def after(node: ASTNode[_]): Unit = {
    node.`type` match {
      case MapToken      => writer.line().outdent().write('}')
      case SequenceToken => writer.line().outdent().write(']')
      case _             =>
    }
  }

  private def visitEntry(entry: ASTNode[_]): Unit = {
    writer.quoted(entry.head.content).write(": ")
    entry.last.accept(this)
  }

  def visitChildren(parent: ASTNode[_]): Unit = {
    var first = true
    parent.children.foreach(c => {
      if (first) {
        c.accept(this)
        first = false
      } else {
        writer.write(',').line()
        c.accept(this)
      }
    })
  }
}
