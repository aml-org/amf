package amf.client.model.domain

import amf.client.convert.CoreClientConverters._
import amf.core.model.domain.{DomainElement => InternalDomainElement, Linkable => InternalLinkable}

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
trait Linkable { this: DomainElement with Linkable =>

  private[amf] def _internal: InternalDomainElement with InternalLinkable

  def linkTarget: ClientOption[DomainElement] = _internal.linkTarget.asClient

  def isLink: Boolean           = _internal.linkTarget.isDefined
  def linkLabel: Option[String] = _internal.linkLabel

  def linkCopy(): Linkable

  def withLinkTarget(target: DomainElement with Linkable): this.type = {
    _internal.withLinkTarget(target._internal)
    this
  }

  def withLinkLabel(label: String): this.type = {
    _internal.withLinkLabel(label)
    this
  }

  def link[T](label: Option[String] = None): T = {
    val copy = linkCopy()
      .withLinkTarget(this)
    label.foreach(copy.withLinkLabel)
    copy.asInstanceOf[T]
  }
}
