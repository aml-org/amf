package amf.model

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class AnyShape(private[amf] val any: amf.shape.AnyShape) extends Shape(any) {
  override private[amf] def element = any
}

@JSExportAll
case class NilShape(private[amf] val nil: amf.shape.NilShape) extends Shape(nil) {
  override private[amf] def element = nil
}

@JSExportAll
abstract class Shape(private[amf] val shape: amf.shape.Shape) extends DomainElement {

  val name: String                    = shape.name
  val displayName: String             = shape.displayName
  val description: String             = shape.description
  val default: String                 = shape.default
  val values: js.Iterable[String]     = shape.values.toJSArray
  val documentation: CreativeWork     = Option(shape.documentation).map(amf.model.CreativeWork).orNull
  val xmlSerialization: XMLSerializer = Option(shape.xmlSerialization).map(amf.model.XMLSerializer).orNull

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
}

object Shape {
  def apply(shape: amf.shape.Shape): Shape =
    (shape match {
      case file: amf.shape.FileShape     => Some(FileShape(file))
      case any: amf.shape.AnyShape       => Some(AnyShape(any))
      case nil: amf.shape.NilShape       => Some(NilShape(nil))
      case node: amf.shape.NodeShape     => Some(NodeShape(node))
      case scalar: amf.shape.ScalarShape => Some(ScalarShape(scalar))
      case array: amf.shape.ArrayShape   => Some(new ArrayShape(array))
      case matrix: amf.shape.MatrixShape => Some(new MatrixShape(matrix.toArrayShape))
      case tuple: amf.shape.TupleShape   => Some(TupleShape(tuple))
      case _                             => None
    }).orNull
}
