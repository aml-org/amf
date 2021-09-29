package amf.apicontract.internal.metamodel.document
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Document}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.document.BaseUnitProcessingDataModel
import amf.core.internal.metamodel.domain.{ModelDoc, ModelVocabularies}

object APIContractProcessingDataModel extends BaseUnitProcessingDataModel {
  val APIContractModelVersion: Field =
    Field(Str,
          ApiContract + "modelVersion",
          ModelDoc(ModelVocabularies.AmlDoc, "modelVersion", "Version of the API contract model"))

  val SourceSpec: Field =
    Field(
      Str,
      ApiContract + "sourceSpec",
      ModelDoc(ModelVocabularies.AmlDoc,
               "sourceSpec",
               "Standard of the original API Specification file (e.g. RAML 1.0, OAS 3.0)")
    )

  override def modelInstance: APIContractProcessingData = APIContractProcessingData()

  override def fields: List[Field] = List(APIContractModelVersion, Transformed, SourceSpec)

  override val `type`: List[ValueType] = List(Document + "APIContractProcessingData")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.AmlDoc,
    "APIContractProcessingData",
    "Class that groups data related to how a Base Unit was processed",
    Seq((Document + "BaseUnitProcessingData").iri())
  )
}