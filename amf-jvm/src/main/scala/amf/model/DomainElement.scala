package amf.model

import scala.collection.JavaConverters._

/**
  * Domain element.
  */
trait DomainElement {

  private[amf] def element: amf.domain.DomainElement

  lazy val customDomainProperties: java.util.List[DomainExtension] =
    element.customDomainProperties.map(DomainExtension).asJava

  def withCustomDomainProperties(customProperties: java.util.List[DomainExtension]): this.type = {
    element.withCustomDomainProperties(customProperties.asScala.map(_.domainExtension))
    this
  }

}
