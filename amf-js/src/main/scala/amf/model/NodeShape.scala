package amf.model

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSConverters._

@JSExportAll
case class NodeShape(private val node: amf.shape.NodeShape) extends Shape(node) {

  val minProperties: Int                     = node.minProperties
  val maxProperties: Int                     = node.maxProperties
  val closed: Boolean                        = node.closed
  val discriminator: String                  = node.discriminator
  val discriminatorValue: String             = node.discriminatorValue
  val readOnly: Boolean                      = node.readOnly
  val properties: js.Iterable[PropertyShape] = node.properties.map(PropertyShape).toJSArray

  def withMinProperties(min: Int): this.type = {
    node.withMinProperties(min)
    this
  }
  def withMaxProperties(max: Int): this.type = {
    node.withMaxProperties(max)
    this
  }
  def withClosed(closed: Boolean): this.type = {
    node.withClosed(closed)
    this
  }
  def withDiscriminator(discriminator: String): this.type = {
    node.withDiscriminator(discriminator)
    this
  }
  def withDiscriminatorValue(value: String): this.type = {
    node.withDiscriminatorValue(value)
    this
  }
  def withReadOnly(readOnly: Boolean): this.type = {
    node.withReadOnly(readOnly)
    this
  }
  def withProperties(properties: js.Iterable[PropertyShape]): this.type = {
    node.withProperties(properties.toList.map(_.propertyShape))
    this
  }

  def withProperty(name: String): PropertyShape = {
    PropertyShape(node.withProperty(name))
  }

}
