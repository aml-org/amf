package amf.shapes.internal.domain.metamodel.core

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.shapes.internal.domain.metamodel.common.DocumentationField
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.client.scala.vocabulary.Namespace.{ApiContract, Core}
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.model.domain.core.ShapeOperation

// TODO: unify with api model:
// Option 1: move part of Operation model to a generic common operation (with request, response, parameters) to core. ApiOperation on api-contract to inherits of that
// Option 2: move all model to amf-model new package. amf-model will contain all objects (including shapes? at least any shape will be needed) That module can be before amf-shapes or event before amf-aml solving the problem of json schema.

object ShapeOperationModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField
    with DocumentationField
    with LinkableElementModel {

  val Method = Field(Str,
                     ApiContract + "method",
                     ModelDoc(ModelVocabularies.ApiContract, "method", "HTTP method required to invoke the operation"))

  val OperationId = Field(Str,
                          ApiContract + "operationId",
                          ModelDoc(ModelVocabularies.ApiContract, "operationId", "Identifier of the target operation"))

  val Accepts = Field(Array(Str),
                      ApiContract + "accepts",
                      ModelDoc(ModelVocabularies.ApiContract, "accepts", "Media-types accepted in a API request"))

  val ContentType = Field(Array(Str),
                          Core + "mediaType",
                          ModelDoc(ModelVocabularies.Core, "mediaType", "Media types returned by a API response"))

  val Request = Field(
    ShapeRequestModel,
    ApiContract + "expects",
    ModelDoc(ModelVocabularies.ApiContract, "expects", "Request information required by the operation"))

  val Responses = Field(Array(ShapeResponseModel),
                        ApiContract + "returns",
                        ModelDoc(ModelVocabularies.ApiContract, "returns", "Response data returned by the operation"))

  override val key: Field = Method

  override val `type`: List[ValueType] = ApiContract + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Method,
    Name,
    Description,
    Documentation,
    Accepts,
    ContentType,
    Request,
    Responses,
    OperationId
  ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = ShapeOperation()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Operation",
    "Action that can be executed using a particular HTTP invocation"
  )
}
