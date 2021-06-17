package amf.apicontract.client.platform.model.domain.security

import amf.core.client.platform.model.StrField
import amf.core.client.platform.model.domain.DomainElement
import amf.core.internal.parser.domain.Annotations
import amf.apicontract.client.scala.model.domain.security.{SecurityRequirement => InternalSecurityRequirement}
import amf.apicontract.internal.convert.ApiClientConverters._

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * ParametrizedSecurityScheme model class.
  */
@JSExportAll
case class SecurityRequirement(override private[amf] val _internal: InternalSecurityRequirement)
    extends DomainElement {

  @JSExportTopLevel("SecurityRequirement")
  def this() = this(InternalSecurityRequirement())

  def name: StrField                                  = _internal.name
  def schemes: ClientList[ParametrizedSecurityScheme] = _internal.schemes.asClient

  /** Set name property of this SecurityRequirement. */
  def withName(name: String): this.type = {
    _internal.withName(name, Annotations.synthesized())
    this
  }

  def withSchemes(schemes: ClientList[ParametrizedSecurityScheme]): this.type = {
    _internal.withSchemes(schemes.asInternal)
    this
  }

  def withScheme(): ParametrizedSecurityScheme = _internal.withScheme()
}
