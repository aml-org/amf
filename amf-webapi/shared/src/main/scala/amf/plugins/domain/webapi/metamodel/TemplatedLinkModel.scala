package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{ApiContract, Core}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.TemplatedLink

/**
  * Link metaModel.
  */
object TemplatedLinkModel
    extends DomainElementModel
    with LinkableElementModel
    with NameFieldSchema
    with DescriptionField {

  val Template = Field(Str,
                       ApiContract + "template",
                       ModelDoc(ModelVocabularies.ApiContract, "template", "URL template for a templated link"))

  val OperationId = Field(Str,
                          ApiContract + "operationId",
                          ModelDoc(ModelVocabularies.ApiContract, "operation ID", "Identifier of the target operation"))

  val OperationRef = Field(Str,
    Http + "operationRef",
    ModelDoc(ModelVocabularies.Http, "operation Ref", "Reference of the target operation"))

  val Mapping = Field(Array(IriTemplateMappingModel),
                      ApiContract + "mapping",
                      ModelDoc(ModelVocabularies.ApiContract, "mapping", "Variable mapping for the URL template"))

  val RequestBody = Field(Str, ApiContract + "requestBody", ModelDoc(ModelVocabularies.ApiContract, "request body", ""))

  val Server = Field(ServerModel, ApiContract + "server", ModelDoc(ModelVocabularies.ApiContract, "server", ""))

  override val `type`: List[ValueType] = ApiContract + "TemplatedLink" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Template :: OperationId :: Mapping :: RequestBody :: Description :: Server :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = TemplatedLink()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Templated Link",
    "Templated link containing URL template and variables mapping"
  )
}
