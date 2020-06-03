package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.{BoolField, IntField, StrField}
import amf.plugins.domain.shapes.models.{NodeShape => InternalNodeShape}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class NodeShape(override private[amf] val _internal: InternalNodeShape) extends AnyShape(_internal) {

  @JSExportTopLevel("model.domain.NodeShape")
  def this() = this(InternalNodeShape())

  def minProperties: IntField                              = _internal.minProperties
  def maxProperties: IntField                              = _internal.maxProperties
  def closed: BoolField                                    = _internal.closed
  def discriminator: StrField                              = _internal.discriminator
  def discriminatorValue: StrField                         = _internal.discriminatorValue
  def discriminatorMapping: ClientList[IriTemplateMapping] = _internal.discriminatorMapping.asClient
  def properties: ClientList[PropertyShape]                = _internal.properties.asClient
  def additionalPropertiesSchema: Shape                    = _internal.additionalPropertiesSchema
  def dependencies: ClientList[PropertyDependencies]       = _internal.dependencies.asClient
  def propertyNames: Shape                                 = _internal.propertyNames

  def withMinProperties(min: Int): this.type = {
    _internal.withMinProperties(min)
    this
  }
  def withMaxProperties(max: Int): this.type = {
    _internal.withMaxProperties(max)
    this
  }
  def withClosed(closed: Boolean): this.type = {
    _internal.withClosed(closed)
    this
  }
  def withDiscriminator(discriminator: String): this.type = {
    _internal.withDiscriminator(discriminator)
    this
  }
  def withDiscriminatorValue(value: String): this.type = {
    _internal.withDiscriminatorValue(value)
    this
  }
  def withDiscriminatorMapping(mappings: ClientList[IriTemplateMapping]): this.type = {
    _internal.withDiscriminatorMapping(mappings.asInternal)
    this
  }
  def withProperties(properties: ClientList[PropertyShape]): this.type = {
    _internal.withProperties(properties.asInternal)
    this
  }
  def withAdditionalPropertiesSchema(additionalPropertiesSchema: Shape): this.type = {
    _internal.withAdditionalPropertiesSchema(additionalPropertiesSchema)
    this
  }
  def withDependencies(dependencies: ClientList[PropertyDependencies]): this.type = {
    _internal.withDependencies(dependencies.asInternal)
    this
  }
  def withPropertyNames(propertyNames: Shape): this.type = {
    _internal.withPropertyNames(propertyNames)
    this
  }

  def withProperty(name: String): PropertyShape = _internal.withProperty(name)

  def withDependency(): PropertyDependencies = _internal.withDependency()

  def withInheritsObject(name: String): NodeShape = _internal.withInheritsObject(name)

  def withInheritsScalar(name: String): ScalarShape = _internal.withInheritsScalar(name)

  override def linkCopy(): NodeShape = _internal.linkCopy()
}
