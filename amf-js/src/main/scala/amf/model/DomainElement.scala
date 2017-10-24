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

  def position(): amf.parser.Range = element.position() match {
    case Some(pos) => pos
    case _         => null
  }

  // API for direct property manipulation

  def getId(): String = element.id

  def getTypeIds(): js.Iterable[String] = element.getTypeIds().toJSArray

  def getPropertyIds(): js.Iterable[String] = element.getPropertyIds().toJSArray

  def getScalarByPropertyId(propertyId: String): js.Iterable[Object] = element.getScalarByPropertyId(propertyId).map(_.asInstanceOf[Object]).toJSArray

  def getObjectByPropertyId(propertyId: String): js.Iterable[DomainElement] = element.getObjectByPropertyId(propertyId).map(d => DomainElement(d)).toJSArray
}

object DomainElement {
  def apply(domainElement: amf.domain.DomainElement) = domainElement match {
    case o: amf.domain.WebApi => WebApi(o)
    case o: amf.domain.Operation => Operation(o)
    case o: amf.domain.Organization => Organization(o)
    case o: amf.domain.ExternalDomainElement => throw new Exception("Not supported yet")
    case o: amf.domain.Parameter => Parameter(o)
    case o: amf.domain.Payload => Payload(o)
    case o: amf.domain.CreativeWork => CreativeWork(o)
    case o: amf.domain.EndPoint => EndPoint(o)
    case o: amf.domain.Request => Request(o)
    case o: amf.domain.Response => Response(o)
    case o: amf.domain.extensions.ObjectNode => ObjectNode(o)
    case o: amf.domain.extensions.ScalarNode => ScalarNode(o)
    case o: amf.domain.extensions.CustomDomainProperty => CustomDomainProperty(o)
    case o: amf.domain.extensions.ArrayNode => ArrayNode(o)
    case o: amf.domain.extensions.DomainExtension => DomainExtension(o)
    case o: amf.shape.Shape => Shape(o)
    case o: amf.domain.dialects.DomainEntity => DomainEntity(o)
    case o => new DomainElement {
      override private[amf] def element = o
    }
  }
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
