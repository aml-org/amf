package amf.apicontract.internal.metamodel.domain.federation

import amf.apicontract.client.scala.model.domain.federation.{EndpointFederationMetadata, ParameterFederationMetadata}
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.federation.FederationMetadataModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object EndpointFederationMetadataModel extends FederationMetadataModel {
  override def modelInstance: EndpointFederationMetadata = EndpointFederationMetadata()

  override val `type`: List[ValueType] = Federation + "EndpointFederationMetadata" :: FederationMetadataModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "EndpointFederationMetadata",
    "Model that contains data about how the Shape should be federated",
    superClasses = Seq((Federation + "FederationMetadata").iri())
  )
}
