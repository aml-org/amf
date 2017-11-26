package amf.model
import amf.model.domain.{AnyShape, NilShape}
import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._

case class AnyShape(private[amf] val any: models.AnyShape) extends Shape(any) {
  override private[amf] def element = any

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.AnyShape => domain.AnyShape(l) })

  override def linkCopy(): DomainElement with Linkable = domain.AnyShape(element.linkCopy())
}

case class NilShape(private[amf] val nil: models.NilShape) extends Shape(nil) {
  override private[amf] def element = nil

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.NilShape => domain.NilShape(l) })

  override def linkCopy(): DomainElement with Linkable = domain.NilShape(element.linkCopy())
}

abstract class Shape(private[amf] val shape: models.Shape) extends DomainElement with Linkable {

  val name: String                      = shape.name
  val displayName: String               = shape.displayName
  val description: String               = shape.description
  val default: String                   = shape.default
  val values: java.util.List[String]    = shape.values.asJava
  val documentation: CreativeWork       = Option(shape.documentation).map(amf.model.CreativeWork).orNull
  val xmlSerialization: XMLSerializer   = Option(shape.xmlSerialization).map(amf.model.XMLSerializer).orNull
  val inherits: java.util.List[Shape]   = shape.inherits.map(Shape(_)).asJava
  val examples: java.util.List[Example] = shape.examples.map(Example).asJava

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
  def withValues(values: Seq[String]): this.type = {
    shape.withValues(values)
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

  def withInherits(inherits: java.util.List[Shape]): this.type = {
    shape.withInherits(inherits.asScala.map(_.shape))
    this
  }

  def withExamples(examples: java.util.List[Example]): this.type = {
    shape.withExamples(examples.asScala.map(_.element))
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
      case array: models.ArrayShape   => Some(ArrayShape(array))
      case matrix: models.MatrixShape => Some(MatrixShape(matrix))
      case tuple: models.TupleShape   => Some(TupleShape(tuple))
      case _                             => None
    }).orNull
}
