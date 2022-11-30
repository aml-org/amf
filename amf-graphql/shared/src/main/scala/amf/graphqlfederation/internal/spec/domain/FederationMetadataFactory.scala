package amf.graphqlfederation.internal.spec.domain

import amf.apicontract.client.scala.model.domain.federation.{EndpointFederationMetadata, ParameterFederationMetadata}
import amf.core.client.scala.model.domain.federation.{FederationMetadata, ShapeFederationMetadata}

trait FederationMetadataFactory[T <: FederationMetadata] {
  def create(): T
}

object ShapeFederationMetadataFactory extends FederationMetadataFactory[ShapeFederationMetadata] {
  override def create(): ShapeFederationMetadata = ShapeFederationMetadata()
}

object ParameterFederationMetadataFactory extends FederationMetadataFactory[ParameterFederationMetadata] {
  override def create(): ParameterFederationMetadata = ParameterFederationMetadata()
}

object EndpointFederationMetadataFactory extends FederationMetadataFactory[EndpointFederationMetadata] {
  override def create(): EndpointFederationMetadata = EndpointFederationMetadata()
}
