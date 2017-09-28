package amf.model

import scala.collection.JavaConverters._

/**
  * Domain element.
  */
trait DomainElement {

  private[amf] def element: amf.domain.DomainElement

  lazy val customDomainProperties: java.util.List[DomainExtension] =
    element.customDomainProperties.map(DomainExtension).asJava
  lazy val extend: java.util.List[ParametrizedDeclaration] =
    element.extend.map(ParametrizedDeclaration(_)).asJava

  def withCustomDomainProperties(customProperties: java.util.List[DomainExtension]): this.type = {
    element.withCustomDomainProperties(customProperties.asScala.map(_.domainExtension))
    this
  }

  def withExtends(extend: java.util.List[ParametrizedDeclaration]): this.type = {
    element.withExtends(extend.asScala.map(_.element))
    this
  }

  def withResourceType(name: String): ParametrizedResourceType =
    ParametrizedResourceType(element.withResourceType(name))

  def withTrait(name: String): ParametrizedTrait = ParametrizedTrait(element.withTrait(name))
}
