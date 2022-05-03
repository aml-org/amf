package amf.apicontract.internal.metamodel.document
import amf.aml.internal.metamodel.document.DialectInstanceProcessingDataModel.SourceSpec
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Document}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.document.BaseUnitProcessingDataModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object APIContractProcessingDataModel extends BaseUnitProcessingDataModel {
  val APIContractModelVersion: Field =
    Field(
      Str,
      ApiContract + "modelVersion",
      ModelDoc(ModelVocabularies.AmlDoc, "modelVersion", "Version of the API contract model")
    )

  override def modelInstance: APIContractProcessingData = APIContractProcessingData()

  override def fields: List[Field] = List(APIContractModelVersion) ++ BaseUnitProcessingDataModel.fields

  override val `type`: List[ValueType] = List(Document + "APIContractProcessingData")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "APIContractProcessingData",
    "Class that groups data related to how a Base Unit was processed",
    Seq((Document + "BaseUnitProcessingData").iri())
  )
}
