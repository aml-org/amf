package amf.generator

import org.yaml.model._

/**
  *
  */
class YamlGenerator {

  private val writer: IndentedWriter = new IndentedWriter

  /** Generate yaml for specified document. */
  def generate(document: YDocument): IndentedWriter = {
    visit(document)
    writer
  }

  def visit(part: YPart, previousSequence: Boolean = false): Unit = {
    part match {
      case document: YDocument =>
        visitChildren(document, forceLine = false)
      case map: YMap =>
        val hasContent = !writer.isEmpty()
        if (!previousSequence && hasContent) writer.indent()
        visitChildren(map, forceLine = !previousSequence && hasContent)
        writer.outdent()
      case seq: YSequence =>
        writer.line().indent()
        visitChildren(seq, "- ", forceLine = false)
        writer.outdent()
      case entry: YMapEntry => visitEntry(entry)
      case node: YNode      => visit(node.value, previousSequence)
      case scalar: YScalar  => writer.write(scalar.text)
      case comment: YComment =>
        writer.write("#" + comment.metaText)
    }
  }

  private def visitEntry(entry: YMapEntry): Unit = {
    visit(entry.key)
    writer.write(": ")
    visit(entry.value)
  }

  def visitChildren(parent: YPart, prefix: String = "", forceLine: Boolean = true): Unit = {
    var first = true
    parent.children
      .filterNot(n => n.isInstanceOf[YIgnorable] && !n.isInstanceOf[YComment])
      .foreach(c => {
        if (!first || forceLine) { writer.line() }
        writer.write(prefix)
        visit(c, previousSequence = parent.isInstanceOf[YSequence])
        first = false
      })
  }
}
