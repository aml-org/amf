package amf.apicontract.client.platform.model.domain.federation

import amf.core.client.platform.model.StrField
import amf.shapes.client.platform.model.domain.NodeShape
import amf.apicontract.client.scala.model.domain.federation.{OperationFederationMetadata => InternalOperationFederationMetadata}
import amf.apicontract.internal.convert.ApiClientConverters._
import amf.core.client.platform.model.domain.federation.FederationMetadata

import scala.scalajs.js.annotation.{JSExportAll, JSExportTopLevel}

@JSExportAll
case class OperationFederationMetadata(override private[amf] val _internal: InternalOperationFederationMetadata)
    extends FederationMetadata {

  @JSExportTopLevel("OperationFederationMetadata")
  def this() = this(InternalOperationFederationMetadata())

  def providedEntity: NodeShape                    = _internal.providedEntity
  def federationMethod: StrField                   = _internal.federationMethod
  def keyMappings: ClientList[ParameterKeyMapping] = _internal.keyMappings.asClient

  def withProvidedEntity(providedEntity: NodeShape): this.type = {
    _internal.withProvidedEntity(providedEntity)
    this
  }
  def withFederationMethod(federationMethod: String): this.type = {
    _internal.withFederationMethod(federationMethod)
    this
  }
  def withKeyMappings(keyMappings: ClientList[ParameterKeyMapping]): this.type = {
    _internal.withKeyMappings(keyMappings.asInternal)
    this
  }

}
