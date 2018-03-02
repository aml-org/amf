package amf.plugins.document.webapi.parser.spec.common

import amf.core.annotations.{DomainExtensionAnnotation, ExplicitField}
import amf.core.metamodel.{Field, Type}
import amf.core.model.domain.extensions.BaseDomainExtension
import amf.core.model.domain.{AmfElement, AmfScalar, Annotation, DomainElement}
import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.RamlValueNode.collectDomainExtensions
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import org.yaml.model._

import scala.collection.mutable.ListBuffer

trait BaseSpecParser extends SpecParserOps {
  implicit val ctx: ParserContext
}

trait SpecParserOps {
  class ObjectField(elem: DomainElement, field: Field)(implicit iv: WebApiContext) extends Function1[YMapEntry, Unit] {

    private val annotations: Annotations = Annotations()

    private var factory: YNode => (ValueNode, Seq[BaseDomainExtension]) = node => (ValueNode(node), Nil)

    /** Expects int, bool or defaults to *text* scalar. */
    private var toElement: YNode => AmfElement = { (node: YNode) =>
      {
        factory(node) match {
          case (v, ext) =>
            val e = toAnnotations(ext)
            field.`type` match {
              case Type.Int  => v.integer().add(e)
              case Type.Bool => v.boolean().add(e)
              case _         => v.text().add(e)
            }
        }
      }
    }

    private def toAnnotations(extensions: Seq[BaseDomainExtension]) =
      Annotations(extensions.map(DomainExtensionAnnotation))

    def allowingAnnotations: ObjectField = {
      factory = node => {
        val n = RamlValueNode(node)
        (n, collectDomainExtensions(elem.id, n))
      }
      this
    }

    def string: ObjectField = {
      this.toElement = n =>
        factory(n) match {
          case (v, e) => v.string().add(toAnnotations(e))
      }
      this
    }

    def negated: ObjectField = {
      this.toElement = n =>
        factory(n) match {
          case (v, e) => v.negated().add(toAnnotations(e))
      }
      this
    }

    def using(fn: (YNode) => AmfElement): ObjectField = {
      toElement = fn
      this
    }

    def explicit: ObjectField = withAnnotation(ExplicitField())

    def withAnnotation(a: Annotation): ObjectField = {
      annotations += a
      this
    }

    override def apply(entry: YMapEntry): Unit = {
      elem.set(field, toElement(entry.value), Annotations(entry) ++= annotations)
    }
  }

  implicit class FieldOps(field: Field)(implicit iv: WebApiContext) {
    def in(elem: DomainElement): ObjectField = new ObjectField(elem, field)
  }
}

/** Scalar valued raml node (based on obj node). */
private case class RamlScalarValuedNode(obj: YMap, scalar: Option[ValueNode])(implicit iv: WebApiContext)
    extends ValueNode {

  override def string(): AmfScalar = as(_.string())

  override def text(): AmfScalar = as(_.text())

  override def integer(): AmfScalar = as(_.integer())

  override def boolean(): AmfScalar = as(_.boolean())

  override def negated(): AmfScalar = as(_.negated())

  private def as(fn: ValueNode => AmfScalar) = scalar.map(fn).getOrElse(AmfScalar(null))
}

object RamlValueNode {
  def apply(node: YNode)(implicit iv: WebApiContext): ValueNode = {
    node.value match {
      case obj: YMap => createScalarValuedNode(obj)
      case _         => ValueNode(node)
    }
  }

  def collectDomainExtensions(parent: String, n: ValueNode)(implicit ctx: WebApiContext): Seq[BaseDomainExtension] = {
    n match {
      case n: RamlScalarValuedNode =>
        AnnotationParser.parseExtensions(s"$parent/oooooo", n.obj)
      case n: ScalarNode =>
        Nil
    }
  }

  private def createScalarValuedNode(obj: YMap)(implicit iv: WebApiContext): RamlScalarValuedNode = {
    var values = ListBuffer[YMapEntry]()

    obj.entries.foreach { entry =>
      entry.key.value match {
        case scalar: YScalar =>
          scalar.text match {
            case "value"                      => values += entry
            case key if isRamlAnnotation(key) => // Valid annotation ;)
            case _                            => unexpected(entry.key)
          }
        case _ => unexpected(entry.key)
      }
    }

    if (values.nonEmpty) {
      values.tail.foreach(d => iv.violation(s"Duplicated key 'value'.", Some(d)))
    }

    RamlScalarValuedNode(obj, values.headOption.map(entry => ValueNode(entry.value)))
  }

  private def unexpected(key: YNode)(implicit iv: WebApiContext): Unit =
    iv.violation(s"Unexpected key '$key'. Options are 'value' or annotations \\(.+\\)", Some(key))
}
