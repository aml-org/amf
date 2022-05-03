package amf.shapes.internal.spec.common.parser

import amf.core.client.scala.model.domain._
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.client.scala.parse.document.ErrorHandlingContext
import amf.core.internal.annotations.{DomainExtensionAnnotation, ExplicitField, SingleValueArray}
import amf.core.internal.metamodel.Type.ArrayLike
import amf.core.internal.metamodel.{Field, Obj, Type}
import amf.core.internal.parser.domain.{ArrayNode, ScalarNode, _}
import WellKnownAnnotation.isRamlAnnotation
import amf.core.internal.datanode.DataNodeParserContext
import amf.core.internal.plugins.syntax.SYamlAMFParserErrorHandler
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.{
  DuplicatedPropertySpecification,
  UnexpectedRamlScalarKey
}
import org.yaml.model._

import scala.collection.mutable.ListBuffer

trait QuickFieldParserOps {
  class ObjectField(target: Target, field: Field)(implicit
      iv: ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler
  ) extends (YMapEntry => Unit) {

    // Custom annotations
    private val custom: Annotations = Annotations()
    // Entry annotations
    private val annotations: Annotations = Annotations()

    private var mapped: Option[YNode => AmfElement]    = None
    private var typed: Option[TypedNode => AmfElement] = None

    private var single    = false
    private var entries   = false
    private var annotated = false
    private var opt       = false

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

    /** Allow parsing map node entries as array elements when expecting array. */
    def treatMapAsArray: ObjectField = {
      entries = true
      this
    }

    /** Optional element. Do not fail on YType.Null. */
    def optional: ObjectField = {
      opt = true
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

    def using(fn: YNode => AmfElement): ObjectField = {
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
      entry.value.tagType match {
        case YType.Null if opt =>
          field.`type` match {
            case ArrayLike(_) if entries =>
              target.foreach(_.set(field, AmfArray(Nil), Annotations(entry) ++= annotations))
            case _ =>
          }
        case _ =>
          val value = field.`type` match {
            case _: Obj if mapped.isDefined => mapped.get(node)
            case ArrayLike(element)         => parseArray(node, element)
            case element                    => parseScalar(node, element, entry.value.tagType)
          }
          value.annotations ++= custom
          target.foreach(_.set(field, value, Annotations(entry) ++= annotations))
      }
    }

    private def parseScalar(node: YNode, element: Type, tagType: YType): AmfElement = {
      val scalar = if (annotated) parseScalarValued(node) else ScalarNode(node)
      (element, tagType) match {
        case _ if typed.isDefined  => typed.get(scalar)
        case (Type.Int, _)         => scalar.integer()
        case (Type.Bool, _)        => scalar.boolean()
        case (Type.Double, _)      => scalar.double()
        case (Type.Str, YType.Str) => scalar.string()
        case _                     => scalar.text()
      }
    }

    private def parseArray(node: YNode, element: Type): AmfElement = {
      val array = createArrayNode(node)
      element match {
        case _: Obj if mapped.isDefined => array.obj(mapped.get)
        case _ if typed.isDefined       => typed.get(array)
        case Type.Int                   => array.integer()
        case Type.Bool                  => array.boolean()
        case _                          => array.text()
      }
    }

    private def createArrayNode(node: YNode): ArrayNode = {
      if (single) SingleArrayNode(node)
      else if (entries) MapArrayNode(node)
      else ArrayNode(node)
    }

    private def parseScalarValued(node: YNode) = {
      val result = RamlScalarNode(node)
      target match {
        case SingleTarget(element) =>
          custom ++= collectDomainExtensions(Some(element), result)
            .map(DomainExtensionAnnotation)
        case EmptyTarget => custom ++= collectDomainExtensions(None, result).map(DomainExtensionAnnotation)
      }
      result
    }

    private def collectDomainExtensions(parent: Option[DomainElement], n: ScalarNode): Seq[DomainExtension] = {
      n match {
        case n: RamlScalarValuedNode =>
          AnnotationParser.parseExtensions(parent, n.obj)
        case _: DefaultScalarNode =>
          Nil
      }
    }
  }

  implicit class FieldOps(field: Field)(implicit
      iv: ErrorHandlingContext with DataNodeParserContext with IllegalTypeHandler
  ) {
    def in(elem: DomainElement): ObjectField = in(SingleTarget(elem))

    def in(target: Target): ObjectField = new ObjectField(target, field)
  }

  trait Target {
    def foreach(fn: DomainElement => Unit)
  }

  private case class SingleTarget(elem: DomainElement) extends Target {
    override def foreach(fn: DomainElement => Unit): Unit = fn(elem)
  }

  case object EmptyTarget extends Target {
    override def foreach(fn: DomainElement => Unit): Unit = {}
  }
}

/** Scalar valued raml node (based on obj node). */
private case class RamlScalarValuedNode(obj: YMap, scalar: ScalarNode)(implicit
    iv: ErrorHandlingContext with IllegalTypeHandler
) extends ScalarNode {

  override def string(): AmfScalar = as(_.string())

  override def text(): AmfScalar = as(_.text())

  override def integer(): AmfScalar = as(_.integer())

  override def double(): AmfScalar = as(_.double())

  override def boolean(): AmfScalar = as(_.boolean())

  override def negated(): AmfScalar = as(_.negated())

  private def as(fn: ScalarNode => AmfScalar) = fn(scalar)
}

private case class RamlSingleArrayNode(node: YNode)(implicit iv: ErrorHandlingContext with IllegalTypeHandler)
    extends ArrayNode {

  override def string(): AmfArray = as(ScalarNode(node).string())

  override def text(): AmfArray = as(ScalarNode(node).text())

  override def integer(): AmfArray = as(ScalarNode(node).integer())

  override def double(): AmfArray = as(ScalarNode(node).double())

  override def boolean(): AmfArray = as(ScalarNode(node).boolean())

  override def negated(): AmfArray = as(ScalarNode(node).negated())

  override def obj(fn: YNode => AmfElement): AmfArray = as(fn(node))

  private def as(element: AmfElement) =
    AmfArray(Seq(element), Annotations.valueNode(node) += SingleValueArray())
}

object SingleArrayNode {
  def apply(node: YNode)(implicit iv: ErrorHandlingContext with IllegalTypeHandler): ArrayNode = {
    node.value match {
      case _: YSequence => ArrayNode(node)
      case _            => RamlSingleArrayNode(node)
    }
  }
}

/** Map array node. */
case class MapEntriesArrayNode(obj: YMap)(override implicit val iv: IllegalTypeHandler) extends BaseArrayNode {

  override def nodes: (Seq[YNode], YNode) = {
    val maps = obj.entries.map(e => YNode(YMap(IndexedSeq(e), obj.sourceName)))
    (maps, obj)
  }
}

object MapArrayNode {
  def apply(node: YNode)(implicit iv: ErrorHandlingContext with IllegalTypeHandler): ArrayNode =
    MapEntriesArrayNode(node.as[YMap])(new SYamlAMFParserErrorHandler(iv.eh))
}

object RamlScalarNode {
  def apply(node: YNode)(implicit iv: ErrorHandlingContext with IllegalTypeHandler): ScalarNode = {
    node.value match {
      case obj: YMap => createScalarValuedNode(obj)
      case _         => ScalarNode(node)
    }
  }

  private def createScalarValuedNode(
      obj: YMap
  )(implicit iv: ErrorHandlingContext with IllegalTypeHandler): RamlScalarValuedNode = {
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
      values.tail.foreach(d =>
        iv.eh.violation(DuplicatedPropertySpecification, "", s"Duplicated key 'value'.", d.location)
      )
    }

    // When an annotated scalar node has no value, the default is an empty node (null).
    RamlScalarValuedNode(
      obj,
      values.headOption
        .map { entry =>
          ScalarNode(entry.value)
        }
        .getOrElse {
          ScalarNode(YNode.Empty)
        }
    )
  }

  private def unexpected(key: YNode)(implicit iv: ErrorHandlingContext): Unit =
    iv.eh.violation(
      UnexpectedRamlScalarKey,
      "",
      s"Unexpected key '$key'. Options are 'value' or annotations \\(.+\\)",
      key.location
    )
}
