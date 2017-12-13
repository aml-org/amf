package amf.model.domain

import amf.core.model.domain.{ExternalDomainElement => SharedExternalDomainElement}

case class ExternalDomainElement(private[amf] val ef: SharedExternalDomainElement) extends DomainElement {
  def this() = this(SharedExternalDomainElement())

  override private[amf] def element: SharedExternalDomainElement = ef
}
