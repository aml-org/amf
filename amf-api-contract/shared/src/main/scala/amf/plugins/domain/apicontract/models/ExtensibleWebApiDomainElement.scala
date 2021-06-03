package amf.plugins.domain.apicontract.models

import amf.core.metamodel.domain.DomainElementModel._
import amf.core.model.domain.DomainElement
import amf.core.utils._
import amf.plugins.domain.apicontract.models.templates.{ParametrizedResourceType, ParametrizedTrait}

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
