package amf.generator

import amf.common.AMFToken._
import amf.parser.{ASTLinkNode, ASTNode}
import amf.visitor.ASTNodeVisitor

/**
  * Created by pedro.colunga on 5/30/17.
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
            case _: MapToken => writer.write('{').line().indent()
            case _: SequenceToken => writer.write('[').line().indent()
            case _ =>
        }
    }

    override def visit(node: ASTNode[_]): Unit = {
        node.`type` match {
            case _: Root | _: MapToken | _: SequenceToken => visitChildren(node)
            case _: Entry => visitEntry(node)
            case _: Number => writer.write(node.content)
            case _: StringToken => writer.quoted(node.content)
            case _ => ???
        }
    }

    override def visit(node: ASTLinkNode[_]): Unit = {
        writer.write('{').line().indent().quoted("$ref").write(": ").quoted(node.target.url).outdent().line().write('}')
    }

    override def after(node: ASTNode[_]): Unit = {
        node.`type` match {
            case _: MapToken => writer.line().outdent().write('}')
            case _: SequenceToken => writer.line().outdent().write(']')
            case _ =>
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
            }
            else {
                writer.write(',').line()
                c.accept(this)
            }
        })
    }
}
