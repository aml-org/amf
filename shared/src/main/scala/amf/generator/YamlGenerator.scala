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

  def visit(part: YPart): Unit = {
    part match {
      case document: YDocument =>
//        visit(part.head)
//        writer.line()
        document.value.foreach(visitChildren(_, forceLine = false))
      case map: YMap =>
        writer.indent()
        visitChildren(map)
        writer.outdent()
      case seq: YSequence =>
        writer.line().indent()
        visitChildren(seq, "-", forceLine = false)
        writer.outdent()
      case entry: YMapEntry => visitEntry(entry)
      case node: YNode      => visit(node.value)
      case scalar: YScalar  => writer.write(' ').write(scalar.text)
    }
  }

  private def visitEntry(entry: YMapEntry): Unit = {
    visit(entry.key)
    writer.write(":")
    visit(entry.value)
  }

  def visitChildren(parent: YPart, prefix: String = "", forceLine: Boolean = true): Unit = {
    var first = true
    parent.children
      .filterNot(_.isInstanceOf[YNonContent])
      .foreach(c => {
        if (!first || forceLine) { writer.line() }
        writer.write(prefix)
        visit(c)
        first = false
      })
  }
}
