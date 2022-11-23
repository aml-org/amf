package amf.apicontract.internal.metamodel.domain.federation

import amf.apicontract.client.scala.model.domain.federation.ParameterFederationMetadata
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.federation.FederationMetadataModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object ParameterFederationMetadataModel extends FederationMetadataModel {
  override def modelInstance: ParameterFederationMetadata = ParameterFederationMetadata()

  override val `type`: List[ValueType] = Federation + "ParameterFederationMetadata" :: FederationMetadataModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "ParameterFederationMetadata",
    "Model that contains data about how the Parameter should be federated",
    superClasses = Seq((Federation + "FederationMetadata").iri())
  )
}
