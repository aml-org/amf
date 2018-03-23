package amf.plugins.domain.webapi.models

import amf.core.metamodel.domain.DomainElementModel._
import amf.core.model.domain.DomainElement
import amf.core.utils._
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}

trait ExtensibleWebApiDomainElement { this: DomainElement =>

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name).withId(id + s"/resourceType/${name.urlEncoded}")
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name).withId(id + s"/trait/${name.urlEncoded}")
    add(Extends, result)
    result
  }
}
