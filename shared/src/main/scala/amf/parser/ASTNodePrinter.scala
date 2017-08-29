package amf.parser

import amf.common.Strings.isNotEmpty
import amf.generator.{IndentedWriter, JsonGenerator}
import amf.lexer.Token.NamedToken
import amf.visitor.ASTNodeVisitor

/**
  *
  */
object ASTNodePrinter {
  def print(node: ASTNode[_]): String = new ASTPrinter().generate(node).toString
}

class ASTPrinter extends ASTNodeVisitor {
  private val writer: IndentedWriter = new IndentedWriter

  /** Generate ast dump for specified document. */
  def generate(root: ASTNode[_]): IndentedWriter = {
    root.accept(this)
    writer
  }

  override def before(node: ASTNode[_]): Unit = {
    node.`type` match {
      case t: NamedToken => writer.write('(').write(t.name).indent()
      case _             => ???
    }
  }

  override def visit(node: ASTNode[_]): Unit = dump(node)

  override def visit(node: ASTLinkNode[_]): Unit = {
    dump(node)
    writer.line().write("target -> ").line().indent()
    if (Option(node.target).isDefined) {
//      generate(node.target.root) TODO include source AST ?
    }
    writer.outdent()
  }

  private def dump(node: ASTNode[_]) = {
    if (node.range != Range.NONE) {
      writer.write(" ").write(node.range.toString)
    }

    if (isNotEmpty(node.content)) {
      writer.line().write("content -> ").write(node.content)
    }

    if (node.children.nonEmpty) {
      writer.write(" {").line().indent()
      node.children.foreach(c => c.accept(this))
      writer.write("}").outdent()
    }
  }

  override def after(node: ASTNode[_]): Unit = {
    node.`type` match {
      case _: NamedToken => writer.write(')').line().outdent()
      case _             =>
    }
  }
}
