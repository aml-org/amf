package amf.plugins.domain.webapi.models

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.DomainElement
import amf.core.parser.Annotations
import amf.plugins.domain.webapi.metamodel.ServerModel.Security
import amf.plugins.domain.webapi.models.security.SecurityRequirement

trait SecuredElement extends DomainElement {

  def security: Seq[SecurityRequirement] = fields.field(Security)

  def withSecurity(security: Seq[SecurityRequirement]): this.type = setArray(Security, security)

  def withSecurity(name: String): SecurityRequirement = {
    val result = SecurityRequirement().withName(name, Annotations() += SynthesizedField())
    add(Security, result)
    result
  }
}
