package amf.apicontract.client.platform.model.domain.federation

import amf.core.client.platform.model.domain.federation.FederationMetadata
import amf.apicontract.client.scala.model.domain.federation.{EndPointFederationMetadata => InternalFederationMetadata}

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class EndPointFederationMetadata(override private[amf] val _internal: InternalFederationMetadata)
    extends FederationMetadata {

  @JSExportTopLevel("EndPointFederationMetadata")
  def this() = this(InternalFederationMetadata())
}
