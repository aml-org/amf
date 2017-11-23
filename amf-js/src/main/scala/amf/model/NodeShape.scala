package amf.model

import amf.plugins.domain.shapes.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class NodeShape(private val node: models.NodeShape) extends Shape(node) {

  val minProperties: Int                              = node.minProperties
  val maxProperties: Int                              = node.maxProperties
  val closed: Boolean                                 = node.closed
  val discriminator: String                           = node.discriminator
  val discriminatorValue: String                      = node.discriminatorValue
  val readOnly: Boolean                               = node.readOnly
  val properties: js.Iterable[PropertyShape]          = node.properties.map(PropertyShape).toJSArray
  val dependencies: js.Iterable[PropertyDependencies] = node.dependencies.map(PropertyDependencies).toJSArray

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

  def withProperty(name: String): PropertyShape = PropertyShape(node.withProperty(name))

  def withDependencies(dependencies: js.Iterable[PropertyDependencies]): this.type = {
    node.withDependencies(dependencies.map(_.element).toSeq)
    this
  }

  def withDependency(): PropertyDependencies = PropertyDependencies(node.withDependency())

  def withInheritsObject(name: String): NodeShape = NodeShape(node.withInheritsObject(name))

  def withInheritsScalar(name: String): ScalarShape = ScalarShape(node.withInheritsScalar(name))

  override private[amf] def element = node

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.NodeShape => NodeShape(l) })

  override def linkCopy(): DomainElement with Linkable = NodeShape(element.linkCopy())
}
