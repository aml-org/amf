package amf.plugins.domain.apicontract.models

import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.Annotations
import amf.plugins.domain.apicontract.metamodel.ServerModel.Security
import amf.plugins.domain.apicontract.models.security.SecurityRequirement

trait SecuredElement extends DomainElement {

  def security: Seq[SecurityRequirement] = fields.field(Security)

  def withSecurity(security: Seq[SecurityRequirement]): this.type = setArray(Security, security)

  def withSecurity(name: String): SecurityRequirement = {
    val result = SecurityRequirement().withName(name, Annotations() += SynthesizedField())
    add(Security, result)
    result
  }
}
