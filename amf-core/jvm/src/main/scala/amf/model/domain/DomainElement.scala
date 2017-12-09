package amf.model.domain

import amf.core.parser.Range
import amf.core.remote.AmfObjectWrapper
import amf.core.unsafe.PlatformSecrets
import amf.core.model.domain

import scala.collection.JavaConverters._

/**
  * Domain element.
  */
trait DomainElement extends AmfObjectWrapper with PlatformSecrets {

  private[amf] def element: domain.DomainElement

  def customDomainProperties =  element.customDomainProperties.map(platform.wrap[CustomDomainProperty](_)).asJava

  def `extends` = element.extend.map(platform.wrap[DomainElement](_)).asJava

  def withCustomDomainProperties(customProperties: java.util.List[DomainExtension]) = {
    element.withCustomDomainProperties(customProperties.asScala.map(_.element))
    this
  }

  def withExtends(extend: java.util.List[ParametrizedDeclaration]) = {
    element.withExtends(extend.asScala.map(_.element))
    this
  }

  def position(): Range = element.position() match {
    case Some(pos) => pos
    case _         => null
  }

  // API for direct property manipulation

  def getId(): String = element.id

  def getTypeIds(): java.util.List[String] = element.getTypeIds().asJava

  def getPropertyIds(): java.util.List[String] = element.getPropertyIds().asJava

  def getScalarByPropertyId(propertyId: String): java.util.List[Object] =
    element.getScalarByPropertyId(propertyId).map(_.asInstanceOf[Object]).asJava

  def getObjectByPropertyId(propertyId: String): java.util.List[DomainElement] =
    element.getObjectByPropertyId(propertyId).map(d => DomainElement(d)).asJava
}

object DomainElement extends PlatformSecrets {
  def apply(domainElement: domain.DomainElement): DomainElement = platform.wrap[DomainElement](domainElement)
}

trait Linkable { this: DomainElement with Linkable =>

  private[amf] def element: domain.DomainElement with domain.Linkable

  def linkTarget: Option[DomainElement with Linkable]

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = element.asInstanceOf[Linkable].linkLabel

  def linkCopy(): DomainElement with Linkable

  def withLinkTarget(target: DomainElement with Linkable): this.type = {
    element.withLinkTarget(target.element)
    this
  }

  def withLinkLabel(label: String): this.type = {
    element.withLinkLabel(label)
    this
  }

  def link[T](label: Option[String] = None): T = {
    val href = linkCopy()
    href.withLinkTarget(this)
    label.map(href.withLinkLabel)

    href.asInstanceOf[T]
  }
}
