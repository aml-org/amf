package amf.model

import amf.plugins.domain.shapes.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class AnyShape(private[amf] val any: models.AnyShape) extends Shape(any) {
  override private[amf] def element = any

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.AnyShape => AnyShape(l) })

  override def linkCopy(): DomainElement with Linkable = AnyShape(element.linkCopy())
}

@JSExportAll
case class NilShape(private[amf] val nil: models.NilShape) extends Shape(nil) {
  override private[amf] def element = nil

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.NilShape => NilShape(l) })

  override def linkCopy(): DomainElement with Linkable = NilShape(element.linkCopy())
}

@JSExportAll
abstract class Shape(private[amf] val shape: models.Shape) extends DomainElement with Linkable {

  val name: String                    = shape.name
  val displayName: String             = shape.displayName
  val description: String             = shape.description
  val default: String                 = shape.default
  val values: js.Iterable[String]     = shape.values.toJSArray
  val documentation: CreativeWork     = Option(shape.documentation).map(amf.model.CreativeWork).orNull
  val xmlSerialization: XMLSerializer = Option(shape.xmlSerialization).map(amf.model.XMLSerializer).orNull
  val inherits: js.Iterable[Shape]    = shape.inherits.map(Shape(_)).toJSArray
  val examples: js.Iterable[Example]  = shape.examples.map(Example).toJSArray

  def withName(name: String): this.type = {
    shape.withName(name)
    this
  }
  def withDisplayName(name: String): this.type = {
    shape.withDisplayName(name)
    this
  }
  def withDescription(description: String): this.type = {
    shape.withDescription(description)
    this
  }
  def withDefault(default: String): this.type = {
    shape.withDefault(default)
    this
  }
  def withValues(values: js.Iterable[String]): this.type = {
    shape.withValues(values.toList)
    this
  }
  def withDocumentation(documentation: CreativeWork): this.type = {
    shape.withDocumentation(documentation.element)
    this
  }

  def withXMLSerialization(xmlSerialization: XMLSerializer): this.type = {
    shape.withXMLSerialization(xmlSerialization.xmlSerializer)
    this
  }

  def withInherits(inherits: js.Iterable[Shape]): this.type = {
    shape.withInherits(inherits.toList.map(_.shape))
    this
  }

  def withExamples(examples: js.Iterable[Example]): this.type = {
    shape.withExamples(examples.toList.map(_.element))
    this
  }
}

object Shape {
  def apply(shape: models.Shape): Shape =
    (shape match {
      case file: models.FileShape     => Some(FileShape(file))
      case any: models.AnyShape       => Some(AnyShape(any))
      case nil: models.NilShape       => Some(NilShape(nil))
      case node: models.NodeShape     => Some(NodeShape(node))
      case scalar: models.ScalarShape => Some(ScalarShape(scalar))
      case array: models.ArrayShape   => Some(new ArrayShape(array))
      case matrix: models.MatrixShape => Some(new MatrixShape(matrix.toArrayShape))
      case tuple: models.TupleShape   => Some(TupleShape(tuple))
      case _                             => None
    }).orNull
}
