package amf.apicontract.client.scala.model.domain

import amf.apicontract.client.scala.model.domain.templates.{ParametrizedResourceType, ParametrizedTrait}
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.metamodel.domain.ShapeModel.Extends
import amf.core.internal.utils.AmfStrings

trait ExtensibleWebApiDomainElement { this: DomainElement =>

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name).withId(id + s"/resourceType/${name.urlComponentEncoded}")
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name).withId(id + s"/trait/${name.urlComponentEncoded}")
    add(Extends, result)
    result
  }
}
