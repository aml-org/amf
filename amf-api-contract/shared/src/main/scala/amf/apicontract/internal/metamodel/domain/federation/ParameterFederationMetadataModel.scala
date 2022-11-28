package amf.apicontract.internal.metamodel.domain.federation

import amf.apicontract.client.scala.model.domain.federation.ParameterFederationMetadata
import amf.core.client.scala.model.domain.federation.ShapeFederationMetadata
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.federation.FederationMetadataModel

object ParameterFederationMetadataModel extends FederationMetadataModel {
  override def modelInstance: ParameterFederationMetadata = ParameterFederationMetadata()

  override val `type`: List[ValueType] = Federation + "ParameterFederationMetadata" :: FederationMetadataModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "ParameterFederationMetadata",
    "Model that contains data about how the Shape should be federated",
    superClasses = Seq((Federation + "FederationMetadata").iri())
  )
}

