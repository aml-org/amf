package amf.model.domain

import amf.core.model.domain.extensions

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}
import scala.scalajs.js.JSConverters._

@JSExportTopLevel("model.domain.CustomDomainProperty")
@JSExportAll
case class CustomDomainProperty(private[amf] val customDomainProperty: extensions.CustomDomainProperty)
    extends DomainElement
    with Linkable {

  def name: String                = customDomainProperty.name
  def displayName: String         = customDomainProperty.displayName
  def description: String         = customDomainProperty.description
  def domain: js.Iterable[String] = Option(customDomainProperty.domain).getOrElse(Nil).toJSArray
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

  def this() = this(extensions.CustomDomainProperty())

  override def element: extensions.CustomDomainProperty = customDomainProperty

  override def linkTarget: Option[DomainElement with Linkable] =
    element.linkTarget.map({ case l: extensions.CustomDomainProperty => CustomDomainProperty(l) })

  override def linkCopy(): DomainElement with Linkable = CustomDomainProperty(element.linkCopy())
}
