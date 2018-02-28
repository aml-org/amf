package amf.plugins.document.webapi.parser.spec.common

import amf.core.model.domain.extensions.DomainScalarExtension
import amf.core.parser._
import amf.core.model.domain.{AmfScalar, DomainElement}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import org.yaml.model._

import scala.collection.mutable.ListBuffer

trait BaseSpecParser {
  implicit val ctx: ParserContext
}

/** Scalar valued raml node (based on obj node). */
private case class RamlScalarValuedNode(obj: YMap, scalar: ValueNode)(implicit iv: WebApiContext) extends ValueNode {

  override def string(): AmfScalar = scalar.string()

  override def text(): AmfScalar = scalar.text()

  override def integer(): AmfScalar = scalar.integer()

  override def boolean(): AmfScalar = scalar.boolean()

  override def negated(): AmfScalar = scalar.negated()

  /** Add custom domain properties of scalar to parent element (if any). */
  override def collectCustomDomainProperties(parent: DomainElement): Unit = {
    AnnotationParser.parseExtensions(s"${parent.id}/titleponele", obj).map { extension =>
      parent.withCustomDomainProperty(
        DomainScalarExtension(extension.fields, extension.annotations).withElement("title"))
    }
  }
}

object RamlValueNode {
  def apply(node: YNode)(implicit iv: WebApiContext): ValueNode = {
    node.value match {
      case obj: YMap => createScalarValuedNode(obj)
      case _         => ValueNode(node)
    }
  }

  private def createScalarValuedNode(obj: YMap)(implicit iv: WebApiContext): RamlScalarValuedNode = {
    var values      = ListBuffer[YMapEntry]()
    var annotations = ListBuffer[YMapEntry]()

    obj.entries.foreach { entry =>
      entry.key.value match {
        case scalar: YScalar =>
          scalar.text match {
            case "value"                      => values += entry
            case key if isRamlAnnotation(key) => annotations += entry
            case _                            => unexpected(entry.key)
          }
        case _ => unexpected(entry.key)
      }
    }

    RamlScalarValuedNode(obj, ValueNode(values.headOption.map(_.value).getOrElse(YNode.Null)))
  }

  private def unexpected(key: YNode)(implicit iv: WebApiContext): Unit =
    iv.violation(s"Unexpected key '$key'. Options are 'value' or annotations \\(.+\\)", Some(key))
}
