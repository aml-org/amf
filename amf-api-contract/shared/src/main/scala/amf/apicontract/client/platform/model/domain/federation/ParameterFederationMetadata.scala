package amf.apicontract.client.platform.model.domain.federation

import amf.core.client.platform.model.domain.federation.FederationMetadata
import amf.apicontract.client.scala.model.domain.federation.{ParameterFederationMetadata => InternalFederationMetadata}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class ParameterFederationMetadata(override private[amf] val _internal: InternalFederationMetadata)
    extends FederationMetadata {

  @JSExportTopLevel("ParameterFederationMetadata")
  def this() = this(InternalFederationMetadata())
}
