package amf.spec

import amf.builder.{Builder, EndPointBuilder}
import amf.domain.Annotation.{LexicalInformation, ParentEndPoint}
import amf.domain.{Annotation, EndPoint}
import amf.metadata.Field
import amf.metadata.domain.APIDocumentationModel.EndPoints
import amf.metadata.domain.EndPointModel.Path
import amf.parser.ASTNode
import amf.spec.Matcher.RegExpMatcher
import amf.common.Strings.strings

import scala.collection.mutable.ListBuffer

/**
  * Spec parsers.
  */
object FieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit

    def apply(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = parse(spec, node, builder)
  }

  object StringValueParser extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      spec.fields.foreach(builder.set(_, entry.last.content.unquote, annotations(entry)))
  }

  case class StringListParser(field: Field*) extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      field.foreach(builder.set(_, entry.last.children.map(c => c.content.unquote), annotations(entry)))
  }

  case class StringJsonListParser(field: Field*) extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      field.foreach(builder.set(_, entry.last.children.map(c => c.head.last.content.unquote), annotations(entry)))
  }

  case class ChildrenParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      entry.last.children.foreach(child => {
        spec.children.find(_.matcher.matches(child)) match {
          case Some(field) => field.parse(field, child, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  case class ChildrenJsonLdParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      entry.last.head.children.foreach(child => {
        spec.children.find(_.matcher.matches(child)) match {
          case Some(field) => field.parse(field, child, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  class EndPointParser() extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val result: ListBuffer[EndPoint] = ListBuffer()
      parse(spec, node, None, result)
      builder.add(EndPoints, result.toList)
    }

    private def parse(spec: SpecField,
                      node: ASTNode[_],
                      parent: Option[EndPoint],
                      collector: ListBuffer[EndPoint]): Unit = {

      val endpoint = EndPointBuilder().set(Path,
                                           parent.map(_.path).getOrElse("") + node.head.content.unquote,
                                           annotations(node.head) :+ ParentEndPoint(parent))
      super.parse(spec, node, endpoint)

      val actual = endpoint.build
      collector += actual

      node.last.children
        .filter(RegExpMatcher("/.*").matches)
        .foreach(parse(spec, _, Some(actual), collector))
    }
  }

  class BuilderParser(b: () => Builder[_], field: Field) extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val innerBuilder: Builder[_] = b()
      super.parse(spec, node, innerBuilder)
      builder.set(field, innerBuilder.build)
    }
  }

  def annotations(node: ASTNode[_]): List[Annotation] = {
    List(LexicalInformation(node.range))
  }
}
