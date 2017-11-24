package amf.model

import amf.plugins.domain.webapi.models

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
case class CustomDomainProperty(private[amf] val customDomainProperty: models.CustomDomainProperty)
    extends DomainElement
    with Linkable {

  def name: String                = customDomainProperty.name
  def displayName: String         = customDomainProperty.displayName
  def description: String         = customDomainProperty.description
  def domain: js.Iterable[String] = customDomainProperty.domain.toJSArray
  def schema: Shape               = Shape(customDomainProperty.schema)

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
  def withDomain(domain: js.Iterable[String]): this.type = {
    customDomainProperty.withDomain(domain.toSeq)
    this
  }

  def withSchema(schema: Shape): this.type = {
    customDomainProperty.withSchema(schema.shape)
    this
  }

  def this() = this(models.CustomDomainProperty())

  override private[amf] def element: models.CustomDomainProperty = customDomainProperty

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: models.CustomDomainProperty => CustomDomainProperty(l) })

  override def linkCopy(): DomainElement with Linkable = CustomDomainProperty(element.linkCopy())
}
