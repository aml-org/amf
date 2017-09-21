package amf.generator

import amf.parser.YValueOps
import org.yaml.model._

/**
  *
  */
class JsonGenerator {

  private val writer: IndentedWriter = new IndentedWriter

  /** Generate json for specified document. */
  def generate(document: YDocument): IndentedWriter = {
    visit(document)
    writer
  }

  def visit(part: YPart): Unit = {
    part match {
      case document: YDocument => visitChildren(document)
      case _: YMap =>
        writer.write('{').line().indent()
        visitChildren(part)
        writer.line().outdent().write('}')
      case _: YSequence =>
        writer.write('[').line().indent()
        visitChildren(part)
        writer.line().outdent().write(']')
      case entry: YMapEntry => visitEntry(entry)
      case node: YNode      => visitNode(node)
    }
  }

  private def visitNode(node: YNode) = {
    node.tag.tagType match {
      case tag @ (YType.Str | YType.Int | YType.Bool | YType.Float) => visitScalar(node.value.toScalar, tag)
      case _                                                        => visit(node.value)
    }
  }

  private def visitScalar(scalar: YScalar, tag: YType) = {
    tag match {
      case YType.Str => writer.quoted(scalar.text)
      case _         => writer.write(scalar.text)
    }
  }

  private def visitEntry(entry: YMapEntry): Unit = {
    visit(entry.key)
    writer.write(": ")
    visit(entry.value)
  }

  def visitChildren(parent: YPart): Unit = {
    var first = true
    parent.children
      .filterNot(_.isInstanceOf[YIgnorable])
      .foreach(c => {
        if (first) {
          visit(c)
          first = false
        } else {
          writer.write(',').line()
          visit(c)
        }
      })
  }
}
