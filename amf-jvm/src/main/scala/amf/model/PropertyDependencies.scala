package amf.model

import amf.plugins.domain.shapes.models

import scala.collection.JavaConverters._

/**
  *
  */
case class PropertyDependencies(private[amf] val property: models.PropertyDependencies) extends DomainElement {

  def propertySource: String                 = property.propertySource
  def propertyTarget: java.util.List[String] = property.propertyTarget.asJava

  def withPropertySource(propertySource: String): this.type = {
    property.withPropertySource(propertySource)
    this
  }

  def withPropertyTarget(propertyTarget: java.util.List[String]): this.type = {
    property.withPropertyTarget(propertyTarget.asScala)
    this
  }

  override private[amf] def element: models.PropertyDependencies = property
}
