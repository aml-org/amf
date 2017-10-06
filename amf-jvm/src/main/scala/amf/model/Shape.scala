package amf.model
import scala.collection.JavaConverters._

case class AnyShape(private[amf] val any: amf.shape.AnyShape) extends Shape(any) {
  override private[amf] def element = any

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: amf.shape.AnyShape => AnyShape(l) })

  override def linkCopy(): DomainElement with Linkable = AnyShape(element.linkCopy())
}

case class NilShape(private[amf] val nil: amf.shape.NilShape) extends Shape(nil) {
  override private[amf] def element = nil

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: amf.shape.NilShape => NilShape(l) })

  override def linkCopy(): DomainElement with Linkable = NilShape(element.linkCopy())
}

abstract class Shape(private[amf] val shape: amf.shape.Shape) extends DomainElement with Linkable {

  val name: String                    = shape.name
  val displayName: String             = shape.displayName
  val description: String             = shape.description
  val default: String                 = shape.default
  val values: java.util.List[String]  = shape.values.asJava
  val documentation: CreativeWork     = Option(shape.documentation).map(amf.model.CreativeWork).orNull
  val xmlSerialization: XMLSerializer = Option(shape.xmlSerialization).map(amf.model.XMLSerializer).orNull
  val inherits: java.util.List[Shape] = shape.inherits.map(Shape(_)).asJava

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

}

object Shape {
  def apply(shape: amf.shape.Shape): Shape =
    (shape match {
      case file: amf.shape.FileShape     => Some(FileShape(file))
      case any: amf.shape.AnyShape       => Some(AnyShape(any))
      case nil: amf.shape.NilShape       => Some(NilShape(nil))
      case node: amf.shape.NodeShape     => Some(NodeShape(node))
      case scalar: amf.shape.ScalarShape => Some(ScalarShape(scalar))
      case array: amf.shape.ArrayShape   => Some(ArrayShape(array))
      case matrix: amf.shape.MatrixShape => Some(MatrixShape(matrix))
      case tuple: amf.shape.TupleShape   => Some(TupleShape(tuple))
      case _                             => None
    }).orNull
}
