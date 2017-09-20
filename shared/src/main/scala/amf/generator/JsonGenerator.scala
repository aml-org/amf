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
      case any              => println(any)
    }
  }

  private def visitNode(node: YNode) = {
    node.tag match {
      case ScalarYTag(tag) => visitScalar(node.value.toScalar, tag)
      case _               => visit(node.value)
    }
  }

  private object ScalarYTag {
    def unapply(tag: YTag): Option[YTag] = {
      tag.tag match {
        case "!!str"   => Some(YTag.Str)
        case "!!int"   => Some(YTag.Int)
        case "!!float" => Some(YTag.Float)
        case "!!bool"  => Some(YTag.Bool)
        case _         => None
      }
    }
  }

  private def visitScalar(scalar: YScalar, tag: YTag) = {
    tag match {
      case t if t.tag == YTag.Str.tag => writer.quoted(scalar.text)
      case _                          => writer.write(scalar.text)
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
      .filterNot(_.isInstanceOf[YNonContent])
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
