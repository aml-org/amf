package amf.plugins.domain.apicontract.metamodel

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{ApiBinding, ApiContract, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.plugins.domain.shapes.metamodel.common.{DocumentationField, ExamplesField}
import amf.plugins.domain.shapes.metamodel.{ExampleModel, NodeShapeModel}
import amf.plugins.domain.apicontract.metamodel.bindings.MessageBindingsModel
import amf.plugins.domain.apicontract.models.Message
import amf.core.internal.metamodel.Type.{Array, Bool, Str}

trait MessageModel
    extends TagsModel
    with ExamplesField
    with DocumentationField
    with AbstractModel
    with NameFieldSchema
    with DescriptionField
    with LinkableElementModel
    with DomainElementModel
    with ParametersFieldModel {
  val Payloads: Field = Field(Array(PayloadModel),
                              ApiContract + "payload",
                              ModelDoc(ModelVocabularies.ApiContract, "payload", "Payload for a Request/Response"))

  val CorrelationId: Field = Field(
    CorrelationIdModel,
    Core + "correlationId",
    ModelDoc(ModelVocabularies.Core,
             "correlationId",
             "An identifier that can be used for message tracing and correlation")
  )

  val DisplayName: Field = Field(Str,
                                 Core + "displayName",
                                 ModelDoc(ModelVocabularies.Core, "displayName", "Human readable name for the term"))

  val Title: Field = Field(Str, Core + "title", ModelDoc(ModelVocabularies.Core, "title", "Title of the item"))

  val Summary: Field = Field(
    Str,
    Core + "summary",
    ModelDoc(ModelVocabularies.Core, "summary", "Human readable short description of the request/response"))

  val Bindings: Field = Field(MessageBindingsModel,
                              ApiBinding + "binding",
                              ModelDoc(ModelVocabularies.ApiBinding, "binding", "Bindings for this request/response"))

  val HeaderExamples: Field = Field(
    Array(ExampleModel),
    ApiContract + "headerExamples",
    ModelDoc(ModelVocabularies.ApiContract, "headerExamples", "Examples for a header definition"))

  val HeaderSchema: Field = Field(
    NodeShapeModel,
    ApiContract + "headerSchema",
    ModelDoc(ModelVocabularies.ApiContract,
             "headerSchema",
             "Object Schema who's properties are headers for the message.")
  )

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
         IsAbstract,
         HeaderExamples,
         HeaderSchema) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "Message" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Message",
    ""
  )

  override def modelInstance: AmfObject = Message()
}
