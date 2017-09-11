package amf.model
import scala.collection.JavaConverters._
abstract class Shape(private[amf] val shape: amf.shape.Shape) {

  val name: String                   = shape.name
  val displayName: String            = shape.displayName
  val description: String            = shape.description
  val default: String                = shape.default
  val values: java.util.List[String] = shape.values.asJava
  val documentation: CreativeWork    = Option(shape.documentation).map(amf.model.CreativeWork).orNull

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
}

object Shape {
  def apply(shape: amf.shape.Shape): Shape =
    (shape match {
      case node: amf.shape.NodeShape     => Some(NodeShape(node))
      case scalar: amf.shape.ScalarShape => Some(ScalarShape(scalar))
      case a                             => None
    }).orNull
}
