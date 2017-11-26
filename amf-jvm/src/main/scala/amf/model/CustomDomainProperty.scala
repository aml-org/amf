package amf.model

import amf.amf.model.domain
import amf.amf.model.domain.CustomDomainProperty
import amf.plugins.domain.webapi.models

import scala.collection.JavaConverters._

case class CustomDomainProperty(private[amf] val customDomainProperty: models.CustomDomainProperty)
    extends DomainElement
    with Linkable {

  def name: String                   = customDomainProperty.name
  def displayName: String            = customDomainProperty.displayName
  def description: String            = customDomainProperty.description
  def domain: java.util.List[String] = customDomainProperty.domain.asJava
  def schema: Shape                  = Shape(customDomainProperty.schema)

  def withName(name: String): this.type = {
    customDomainProperty.withName(name)
    this
  }

  def withDisplayName(displayName: String): this.type = {
    customDomainProperty.withDisplayName(displayName)
    this
  }

  def withDescription(description: String): this.type = {
    customDomainProperty.withDescription(description)
    this
  }
  def withDomain(domain: java.util.List[String]): this.type = {
    customDomainProperty.withDomain(domain.asScala)
    this
  }

  def withSchema(schema: Shape): this.type = {
    customDomainProperty.withSchema(schema.shape)
    this
  }

  def this() = this(models.CustomDomainProperty())

  override private[amf] def element: models.CustomDomainProperty = customDomainProperty

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.CustomDomainProperty => domain.CustomDomainProperty(l) })

  override def linkCopy(): DomainElement with Linkable = CustomDomainProperty(element.linkCopy())
}
