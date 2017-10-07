package amf.model

import amf.domain.{Annotations, Fields}

import scala.collection.JavaConverters._

case class NodeShape(private val node: amf.shape.NodeShape) extends Shape(node) {

  val minProperties: Int                                 = node.minProperties
  val maxProperties: Int                                 = node.maxProperties
  val closed: Boolean                                    = node.closed
  val discriminator: String                              = node.discriminator
  val discriminatorValue: String                         = node.discriminatorValue
  val readOnly: Boolean                                  = node.readOnly
  val properties: java.util.List[PropertyShape]          = node.properties.map(PropertyShape).asJava
  val dependencies: java.util.List[PropertyDependencies] = node.dependencies.map(PropertyDependencies).asJava

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
  def withProperties(properties: java.util.List[PropertyShape]): this.type = {
    node.withProperties(properties.asScala.map(_.propertyShape))
    this
  }

  def withProperty(name: String): PropertyShape = PropertyShape(node.withProperty(name))

  def withDependencies(dependencies: java.util.List[PropertyDependencies]): this.type = {
    node.withDependencies(dependencies.asScala.map(_.element))
    this
  }

  def withDependency(): PropertyDependencies = PropertyDependencies(node.withDependency())

  def withInheritsObject(name: String): NodeShape = NodeShape(node.withInheritsObject(name))

  def withInheritsScalar(name: String): ScalarShape = ScalarShape(node.withInheritsScalar(name))

  override private[amf] def element = node

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: amf.shape.NodeShape => NodeShape(l) })

  override def linkCopy(): DomainElement with Linkable = NodeShape(element.linkCopy())
}
