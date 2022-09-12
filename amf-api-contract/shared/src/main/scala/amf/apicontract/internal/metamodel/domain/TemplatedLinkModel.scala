package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.TemplatedLink
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescribedElementModel, NameFieldSchema}
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.IriTemplateMappingModel

/** Link metaModel.
  */
object TemplatedLinkModel
    extends DomainElementModel
    with LinkableElementModel
    with NameFieldSchema
    with DescribedElementModel {

  val Template = Field(
    Str,
    ApiContract + "template",
    ModelDoc(ModelVocabularies.ApiContract, "template", "URL template for a templated link")
  )

  val OperationId = Field(
    Str,
    ApiContract + "operationId",
    ModelDoc(ModelVocabularies.ApiContract, "operationId", "Identifier of the target operation")
  )

  val OperationRef = Field(
    Str,
    ApiContract + "operationRef",
    ModelDoc(ModelVocabularies.ApiContract, "operationRef", "Reference of the target operation")
  )

  val Mapping = Field(
    Array(IriTemplateMappingModel),
    ApiContract + "mapping",
    ModelDoc(ModelVocabularies.ApiContract, "mapping", "Variable mapping for the URL template")
  )

  val RequestBody =
    Field(Str, ApiContract + "requestBody", ModelDoc(ModelVocabularies.ApiContract, "requestBody", ""))

  val Server = Field(ServerModel, ApiContract + "server", ModelDoc(ModelVocabularies.ApiContract, "server", ""))

  override val `type`: List[ValueType] = ApiContract + "TemplatedLink" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Template :: OperationId :: Mapping :: RequestBody :: Description :: Server :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = TemplatedLink()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "TemplatedLink",
    "Templated link containing URL template and variables mapping"
  )
}
