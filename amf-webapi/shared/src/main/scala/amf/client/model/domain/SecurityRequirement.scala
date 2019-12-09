package amf.client.model.domain

import amf.client.convert.WebApiClientConverters._
import amf.client.model.StrField
import amf.plugins.domain.webapi.models.security.{SecurityRequirement => InternalSecurityRequirement}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

/**
  * ParametrizedSecurityScheme model class.
  */
@JSExportAll
case class SecurityRequirement(override private[amf] val _internal: InternalSecurityRequirement)
    extends DomainElement {

  @JSExportTopLevel("model.domain.SecurityRequirement")
  def this() = this(InternalSecurityRequirement())

  def name: StrField                                  = _internal.name
  def schemes: ClientList[ParametrizedSecurityScheme] = _internal.schemes.asClient

  /** Set name property of this SecurityRequirement. */
  def withName(name: String): this.type = {
    _internal.withName(name)
    this
  }

  def withSchemes(schemes: ClientList[ParametrizedSecurityScheme]): this.type = {
    _internal.withSchemes(schemes.asInternal)
    this
  }

  def withScheme(): ParametrizedSecurityScheme = _internal.withScheme()
}
