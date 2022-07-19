package amf.apicontract.internal.metamodel.domain.federation

import amf.apicontract.client.scala.model.domain.federation.OperationFederationMetadata
import amf.core.client.scala.vocabulary.Namespace.Federation
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.federation.FederationMetadataModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.NodeShapeModel

object OperationFederationMetadataModel extends FederationMetadataModel {

  val ProvidedEntity: Field = Field(
    NodeShapeModel,
    Federation + "providedEntity",
    ModelDoc(
      ModelVocabularies.Federation,
      "providedEntity",
      "Node shape provided by this Operation to the federated graph"
    )
  )

  val FederationMethod: Field = Field(
    Str,
    Federation + "federationMethod",
    ModelDoc(
      ModelVocabularies.Federation,
      "federationMethod",
      "REST method (e.g. GET, POST) used to retrieve an entity. Overrides the Method from the current operation"
    )
  )

  val KeyMappings: Field = Field(
    Array(ParameterKeyMappingModel),
    Federation + "keyMappings",
    ModelDoc(
      ModelVocabularies.Federation,
      "keyMapping",
      "Mapping from parameters to properties from the provided entity to be used for data retrieval"
    )
  )

  override val fields: List[Field] = ProvidedEntity :: FederationMethod :: KeyMappings :: FederationMetadataModel.fields

  override def modelInstance: OperationFederationMetadata = OperationFederationMetadata()

  override val `type`: List[ValueType] = Federation + "OperationFederationMetadata" :: FederationMetadataModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Federation,
    "OperationFederationMetadata",
    "Model that contains data about how the Operation is used in a federated graph",
    superClasses = Seq((Federation + "FederationMetadata").iri())
  )
}
