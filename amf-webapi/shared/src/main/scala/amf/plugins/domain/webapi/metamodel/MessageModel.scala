package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.{ApiBinding, ApiContract, Core}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.{DocumentationField, ExamplesField}
import amf.plugins.domain.webapi.metamodel.bindings.MessageBindingModel

trait MessageModel
    extends TagsModel
    with ExamplesField
    with DocumentationField
    with AbstractModel
    with NameFieldSchema
    with DescriptionField
    with LinkableElementModel
    with ParametersFieldModel {
  val Payloads = Field(Array(PayloadModel),
                       ApiContract + "payload",
                       ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  val CorrelationId = Field(
    CorrelationIdModel,
    Core + "correlationId",
    ModelDoc(ModelVocabularies.Core,
             "correlationId",
             "an identifier that can be used for message tracing and correlation")
  )

  val DisplayName = Field(Str,
                          Core + "displayName",
                          ModelDoc(ModelVocabularies.Core, "displayName", "Human readable name for the term"))

  val Title = Field(Str, Core + "title", ModelDoc(ModelVocabularies.Core, "title", "Title of the item"))

  val Summary = Field(
    Str,
    Core + "summary",
    ModelDoc(ModelVocabularies.Core, "summary", "Human readable short description of the request/response"))

  val Bindings = Field(Array(MessageBindingModel),
                       ApiBinding + "binding",
                       ModelDoc(ModelVocabularies.ApiBinding, "binding", "Bindings for this request/response"))

}

object MessageModel extends MessageModel {
  val fields: List[Field] =
    List(Name,
         Description,
         Payloads,
         CorrelationId,
         DisplayName,
         Title,
         Summary,
         Headers,
         Bindings,
         Tags,
         Examples,
         Documentation,
         IsAbstract) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "Message" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Message",
    ""
  )
}
