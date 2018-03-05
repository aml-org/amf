package amf.plugins.document.webapi.parser.spec.common

import amf.core.annotations.{DomainExtensionAnnotation, ExplicitField, SingleValueArray}
import amf.core.metamodel.Type.ArrayLike
import amf.core.metamodel.{Field, Obj, Type}
import amf.core.model.domain.extensions.DomainExtension
import amf.core.model.domain.{ArrayNode => _, ScalarNode => _, _}
import amf.core.parser._
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.RamlScalarNode.collectDomainExtensions
import amf.plugins.document.webapi.parser.spec.common.WellKnownAnnotation.isRamlAnnotation
import org.yaml.model._

import scala.collection.mutable.ListBuffer

trait BaseSpecParser extends SpecParserOps {
  implicit val ctx: ParserContext
}

trait SpecParserOps {

  class ObjectField(elem: DomainElement, field: Field)(implicit iv: WebApiContext) extends Function1[YMapEntry, Unit] {

    private val annotations: Annotations = Annotations()

    private var mapped: Option[YNode => AmfElement]    = None
    private var typed: Option[TypedNode => AmfElement] = None

    private var single    = false
    private var annotated = false
    private var dry       = false

    /** Allow scalar-valued annotations. */
    def allowingAnnotations: ObjectField = {
      annotated = true
      this
    }

    /** Allow parsing a single node when expecting array. */
    def allowingSingleValue: ObjectField = {
      single = true
      this
    }

    /** Dry run: do not update object, only parse element. */
    def parseOnly: ObjectField = {
      dry = true
      this
    }

    def string: ObjectField = {
      typed = Some(_.string())
      this
    }

    def negated: ObjectField = {
      typed = Some(_.negated())
      this
    }

    def using(fn: (YNode) => AmfElement): ObjectField = {
      mapped = Some(fn)
      this
    }

    def explicit: ObjectField = withAnnotation(ExplicitField())

    def withAnnotation(a: Annotation): ObjectField = {
      annotations += a
      this
    }

    override def apply(entry: YMapEntry): Unit = {
      val node = entry.value
      val value = field.`type` match {
        case _: Obj if mapped.isDefined => mapped.get(node)
        case ArrayLike(element)         => parseArray(node, element)
        case element                    => parseScalar(node, element)
      }
      if (!dry) elem.set(field, value, Annotations(entry) ++= annotations)
    }

    private def parseScalar(node: YNode, element: Type): AmfElement = {
      val scalar = if (annotated) parseScalarValued(node) else ScalarNode(node)
      element match {
        case _ if typed.isDefined => typed.get(scalar)
        case Type.Int             => scalar.integer()
        case Type.Bool            => scalar.boolean()
        case _                    => scalar.text()
      }
    }

    private def parseArray(node: YNode, element: Type): AmfElement = {
      val array = if (single) SingleArrayNode(node) else ArrayNode(node)
      element match {
        case _: Obj if mapped.isDefined => array.obj(mapped.get)
        case _ if typed.isDefined       => typed.get(array)
        case Type.Int                   => array.integer()
        case Type.Bool                  => array.boolean()
        case _                          => array.text()
      }
    }

    private def parseScalarValued(node: YNode) = {
      val result = RamlScalarNode(node)
      annotations ++= Annotations(collectDomainExtensions(elem.id, result).map(DomainExtensionAnnotation))
      result
    }
  }

  implicit class FieldOps(field: Field)(implicit iv: WebApiContext) {
    def in(elem: DomainElement): ObjectField = new ObjectField(elem, field)
  }
}

/** Scalar valued raml node (based on obj node). */
private case class RamlScalarValuedNode(obj: YMap, scalar: Option[ScalarNode])(implicit iv: WebApiContext)
    extends ScalarNode {

  override def string(): AmfScalar  = as(_.string())
  override def text(): AmfScalar    = as(_.text())
  override def integer(): AmfScalar = as(_.integer())
  override def boolean(): AmfScalar = as(_.boolean())
  override def negated(): AmfScalar = as(_.negated())

  private def as(fn: ScalarNode => AmfScalar) = scalar.map(fn).getOrElse(AmfScalar(null))
}

private case class RamlSingleArrayNode(node: YNode)(implicit iv: WebApiContext) extends ArrayNode {

  override def string(): AmfArray                     = as(ScalarNode(node).string())
  override def text(): AmfArray                       = as(ScalarNode(node).text())
  override def integer(): AmfArray                    = as(ScalarNode(node).integer())
  override def boolean(): AmfArray                    = as(ScalarNode(node).boolean())
  override def negated(): AmfArray                    = as(ScalarNode(node).negated())
  override def obj(fn: YNode => AmfElement): AmfArray = as(fn(node))

  private def as(element: AmfElement) =
    AmfArray(Seq(element), Annotations(node.value) += SingleValueArray())
}

object SingleArrayNode {
  def apply(node: YNode)(implicit iv: WebApiContext): ArrayNode = {
    node.value match {
      case obj: YSequence => ArrayNode(obj)
      case _              => RamlSingleArrayNode(node)
    }
  }
}

object RamlScalarNode {
  def apply(node: YNode)(implicit iv: WebApiContext): ScalarNode = {
    node.value match {
      case obj: YMap => createScalarValuedNode(obj)
      case _         => ScalarNode(node)
    }
  }

  def collectDomainExtensions(parent: String, n: ScalarNode)(implicit ctx: WebApiContext): Seq[DomainExtension] = {
    n match {
      case n: RamlScalarValuedNode =>
        AnnotationParser.parseExtensions(s"$parent/oooooo", n.obj)
      case n: DefaultScalarNode =>
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

    RamlScalarValuedNode(obj, values.headOption.map(entry => ScalarNode(entry.value)))
  }

  private def unexpected(key: YNode)(implicit iv: WebApiContext): Unit =
    iv.violation(s"Unexpected key '$key'. Options are 'value' or annotations \\(.+\\)", Some(key))
}
