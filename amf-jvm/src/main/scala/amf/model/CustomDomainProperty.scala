package amf.model

import scala.collection.JavaConverters._

case class CustomDomainProperty(private[amf] val customDomainProperty: amf.domain.extensions.CustomDomainProperty) extends DomainElement {

  def name: String                   = customDomainProperty.name
  def description: String            = customDomainProperty.description
  def domain: java.util.List[String] = customDomainProperty.domain.asJava
  def schema: Shape                  = Shape(customDomainProperty.schema)

  def withName(name: String): this.type = {
    customDomainProperty.withName(name)
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
