package amf.model

// import amf.model.DomainExtension

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Domain element.
  */
trait DomainElement {
  private[amf] def element: amf.domain.DomainElement

  lazy val customDomainProperties: js.Iterable[DomainExtension] =
    element.customDomainProperties.map(DomainExtension).toJSArray
  lazy val extend: js.Iterable[ParametrizedDeclaration] =
    element.extend.map(ParametrizedDeclaration(_)).toJSArray

  def withCustomDomainProperties(customProperties: js.Iterable[DomainExtension]): this.type = {
    element.withCustomDomainProperties(customProperties.map(_.domainExtension).toSeq)
    this
  }

  def withExtends(extend: js.Iterable[ParametrizedDeclaration]): this.type = {
    element.withExtends(extend.map(_.element).toSeq)
    this
  }

  def withResourceType(name: String): ParametrizedResourceType =
    ParametrizedResourceType(element.withResourceType(name))

  def withTrait(name: String): ParametrizedTrait = ParametrizedTrait(element.withTrait(name))
}

trait Linkable { this: DomainElement with Linkable =>

  private[amf] def element: amf.domain.DomainElement with amf.domain.Linkable

  def linkTarget: Option[DomainElement with Linkable]

  def isLink: Boolean           = linkTarget.isDefined
  def linkLabel: Option[String] = element.linkLabel

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
