package amf.model

// import amf.model.DomainExtension

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
  * Domain element.
  */
trait DomainElement {
  private[amf] def element: amf.domain.DomainElement

  val customDomainProperties: js.Iterable[DomainExtension] = element.customDomainProperties.map(DomainExtension(_)).toJSArray

  def  withCustomDomainProperties(customProperties: js.Iterable[DomainExtension]): this.type = {
    element.withCustomDomainProperties(customProperties.map(_.domainExtension).toSeq)
    this
  }

}
