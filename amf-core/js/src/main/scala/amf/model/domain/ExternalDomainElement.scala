package amf.model.domain

import scala.scalajs.js.annotation.JSExportAll

import amf.core.model.domain.{ExternalDomainElement => SharedExternalDomainElement}

@JSExportAll
case class ExternalDomainElement(private[amf] val ef: SharedExternalDomainElement) extends DomainElement {
  def this() = this(SharedExternalDomainElement())

  def raw(): String = ef.raw
  def withRaw(raw: String) = {
    ef.withRaw(raw)
    this
  }

  override private[amf] def element: SharedExternalDomainElement = ef
}
