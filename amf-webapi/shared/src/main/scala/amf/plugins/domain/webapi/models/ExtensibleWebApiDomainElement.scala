package amf.plugins.domain.webapi.models

import amf.core.metamodel.domain.DomainElementModel._
import amf.core.model.domain.DomainElement
import amf.plugins.domain.webapi.models.templates.{ParametrizedResourceType, ParametrizedTrait}

trait ExtensibleWebApiDomainElement { this: DomainElement =>

  def withResourceType(name: String): ParametrizedResourceType = {
    val result = ParametrizedResourceType().withName(name)
    add(Extends, result)
    result
  }

  def withTrait(name: String): ParametrizedTrait = {
    val result = ParametrizedTrait().withName(name)
    add(Extends, result)
    result
  }
}
