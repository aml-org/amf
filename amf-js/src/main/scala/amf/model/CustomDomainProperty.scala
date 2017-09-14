package amf.model

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportAll
import scala.scalajs.js.JSConverters._

@JSExportAll
case class CustomDomainProperty(private[amf] val customDomainProperty: amf.domain.extensions.CustomDomainProperty) extends DomainElement {

  def name: String                = customDomainProperty.name
  def description: String         = customDomainProperty.description
  def domain: js.Iterable[String] = customDomainProperty.domain.toJSArray
  def schema: Shape               = Shape(customDomainProperty.schema)

  def withName(name: String): this.type = {
    customDomainProperty.withName(name)
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


  def this() = this(amf.domain.extensions.CustomDomainProperty())

  override def equals(other: Any): Boolean = other match {
    case that: CustomDomainProperty =>
      (that canEqual this) &&
        customDomainProperty == that.customDomainProperty
    case _ => false
  }

  def canEqual(other: Any): Boolean = other.isInstanceOf[CustomDomainProperty]

  override private[amf] def element: amf.domain.extensions.CustomDomainProperty = customDomainProperty
}
