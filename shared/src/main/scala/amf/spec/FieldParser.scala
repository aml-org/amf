package amf.spec

import amf.builder._
import amf.common.Strings.strings
import amf.domain.Annotation.{LexicalInformation, ParentEndPoint}
import amf.domain.{Annotation, EndPoint}
import amf.metadata.Type
import amf.metadata.domain.APIDocumentationModel.EndPoints
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain._
import amf.parser.ASTNode
import amf.spec.Matcher.RegExpMatcher

import scala.collection.mutable.ListBuffer

/**
  * Spec parsers.
  */
object FieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit

    def apply(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = parse(spec, node, builder)
  }

  trait ValueParser[T] extends SpecFieldParser {
    def value(content: String): T

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      spec.fields.foreach(builder.set(_, value(entry.last.content.unquote), annotations(entry)))
    }
  }

  object StringValueParser extends ValueParser[String] {
    override def value(content: String): String = content
  }

  object BoolValueParser extends ValueParser[Boolean] {
    override def value(content: String): Boolean = content.toBoolean
  }

  object StringListParser extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      spec.fields.foreach(builder.set(_, entry.last.children.map(c => c.content.unquote), annotations(entry)))
  }

  case class ChildrenParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      entry.last.children.foreach(child => {
        spec.children.find(_.matcher.matches(child)) match {
          case Some(field) => field.parser(field, child, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  object EndPointParser extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val result: ListBuffer[EndPoint] = ListBuffer()
      parse(spec, node, None, result)
      builder.add(EndPoints, result.toList)
    }

    private def parse(spec: SpecField,
                      node: ASTNode[_],
                      parent: Option[EndPoint],
                      collector: ListBuffer[EndPoint]): Unit = {

      val annotation = annotations(node.head)
      val endpoint = EndPointBuilder().set(
        Path,
        parent.map(_.path).getOrElse("") + node.head.content.unquote,
        if (parent.isDefined) annotation :+ ParentEndPoint(parent.get) else annotation)
      super.parse(spec, node, endpoint)

      val actual = endpoint.build
      collector += actual

      node.last.children
        .filter(RegExpMatcher("/.*").matches)
        .foreach(parse(spec, _, Some(actual), collector))
    }
  }

  object OperationParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      val b = OperationBuilder().set(OperationModel.Method, entry.head.content.unquote, annotations(entry))
      super.parse(spec, entry, b)
      builder add (EndPointModel.Operations, List(b.build))
    }
  }

  object ObjectParser extends ChildrenParser {

    def builderForType(t: Type): Builder[_] = t match {
      case OrganizationModel => OrganizationBuilder()
      case LicenseModel      => LicenseBuilder()
      case CreativeWorkModel => CreativeWorkBuilder()
    }

    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val field                    = spec.fields.head
      val innerBuilder: Builder[_] = builderForType(field.`type`)
      super.parse(spec, node, innerBuilder)
      builder.set(field, innerBuilder.build)
    }
  }

  def annotations(node: ASTNode[_]): List[Annotation] = {
    List(LexicalInformation(node.range))
  }
}
