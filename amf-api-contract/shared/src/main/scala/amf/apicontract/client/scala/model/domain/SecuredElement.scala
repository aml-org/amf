package amf.apicontract.client.scala.model.domain

import amf.apicontract.client.scala.model.domain.security.SecurityRequirement
import amf.apicontract.internal.metamodel.domain.ServerModel.Security
import amf.core.client.scala.model.domain.DomainElement
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.parser.domain.Annotations

trait SecuredElement extends DomainElement {

  def security: Seq[SecurityRequirement] = fields.field(Security)

  def withSecurity(security: Seq[SecurityRequirement]): this.type = setArray(Security, security)

  def withSecurity(name: String): SecurityRequirement = {
    val result = SecurityRequirement().withName(name, Annotations() += SynthesizedField())
    add(Security, result)
    result
  }
}
