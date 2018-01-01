package amf.model.domain

import amf.core.model.domain
import amf.core.parser.Range
import amf.core.remote.AmfObjectWrapper
import amf.core.unsafe.PlatformSecrets

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * Domain element.
  */
@JSExportAll
@JSExportTopLevel("model.domain.DomainElement")
class DomainElement extends AmfObjectWrapper with PlatformSecrets {

  private[amf] def element: domain.DomainElement = throw new Exception("DomainElement is abstract")

  def customDomainProperties = element.customDomainProperties.map(platform.wrap[CustomDomainProperty](_)).toJSArray

  def `extends` = element.extend.map(platform.wrap[DomainElement](_)).toJSArray

  def withCustomDomainProperties(customProperties: js.Iterable[DomainExtension]) = {
    element.withCustomDomainProperties(customProperties.map(_.element).toSeq)
    this
  }

  def withExtends(extend: js.Iterable[ParametrizedDeclaration]) = {
    element.withExtends(extend.map(_.element).toSeq)
    this
  }

  def position(): Range = element.position() match {
    case Some(pos) => pos
    case _         => null
  }

  // API for direct property manipulation

  def getId(): String = element.id
  def withId(id: String) = {
    element.withId(id)
    this
  }

  def getTypeIds(): js.Iterable[String] = element.getTypeIds().toJSArray

  def getPropertyIds(): js.Iterable[String] = element.getPropertyIds().toJSArray

  def getScalarByPropertyId(propertyId: String): js.Iterable[Object] =
    element.getScalarByPropertyId(propertyId).map(_.asInstanceOf[Object]).toJSArray

  def getObjectByPropertyId(propertyId: String): js.Iterable[DomainElement] =
    element.getObjectByPropertyId(propertyId).map(d => DomainElement(d)).toJSArray
}

object DomainElement extends PlatformSecrets {
  def apply(domainElement: domain.DomainElement): DomainElement = platform.wrap[DomainElement](domainElement)
}

@JSExportAll
trait Linkable { this: DomainElement with Linkable =>

  def linkTarget: Option[DomainElement with Linkable]

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = element.asInstanceOf[domain.Linkable].linkLabel

  def linkCopy(): DomainElement with Linkable

  def withLinkTarget(target: DomainElement with Linkable): this.type = {
    element.asInstanceOf[domain.Linkable].withLinkTarget(target.element)
    this
  }

  def withLinkLabel(label: String): this.type = {
    element.asInstanceOf[domain.Linkable].withLinkLabel(label)
    this
  }

  def link[T](label: Option[String] = None): T = {
    val href = linkCopy()
    href.withLinkTarget(this)
    label.map(href.withLinkLabel)

    href.asInstanceOf[T]
  }
}
