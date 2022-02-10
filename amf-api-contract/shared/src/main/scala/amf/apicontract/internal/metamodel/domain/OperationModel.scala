package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.internal.metamodel.domain.bindings.OperationBindingsModel
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel
import amf.core.client.scala.vocabulary.Namespace.{ApiBinding, ApiContract, ApiFederation, Core}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Bool, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.federation.EntityProviderModel
import amf.core.internal.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.common.DocumentationField

/**
  * Operation meta model.
  */
object OperationModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField
    with DocumentationField
    with TagsModel
    with AbstractModel
    with LinkableElementModel {

  val Method = Field(Str,
                     ApiContract + "method",
                     ModelDoc(ModelVocabularies.ApiContract, "method", "HTTP method required to invoke the operation"))

  val OperationId = Field(Str,
                          ApiContract + "operationId",
                          ModelDoc(ModelVocabularies.ApiContract, "operationId", "Identifier of the target operation"))

  val Deprecated = Field(Bool,
                         Core + "deprecated",
                         ModelDoc(ModelVocabularies.Core, "deprecated", "Marks the operation as deprecated"))

  val Summary = Field(
    Str,
    ApiContract + "guiSummary",
    ModelDoc(ModelVocabularies.ApiContract,
             "guiSummary",
             "Human readable description of the operation",
             Seq((Namespace.Core + "description").iri()))
  )

  val Schemes =
    Field(Array(Str),
          ApiContract + "scheme",
          ModelDoc(ModelVocabularies.ApiContract, "scheme", "URI scheme for the API protocol"))

  val Accepts = Field(Array(Str),
                      ApiContract + "accepts",
                      ModelDoc(ModelVocabularies.ApiContract, "accepts", "Media-types accepted in a API request"))

  val ContentType = Field(Array(Str),
                          Core + "mediaType",
                          ModelDoc(ModelVocabularies.Core, "mediaType", "Media types returned by a API response"))

  val Request = Field(
    Array(RequestModel),
    ApiContract + "expects",
    ModelDoc(ModelVocabularies.ApiContract, "expects", "Request information required by the operation"))

  val Responses = Field(Array(ResponseModel),
                        ApiContract + "returns",
                        ModelDoc(ModelVocabularies.ApiContract, "returns", "Response data returned by the operation"))

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Security schemes applied to an element in the API spec")
  )

  val Callbacks = Field(Array(CallbackModel),
                        ApiContract + "callback",
                        ModelDoc(ModelVocabularies.ApiContract, "callback", "Associated callbacks"))

  val Servers =
    Field(Array(ServerModel),
          ApiContract + "server",
          ModelDoc(ModelVocabularies.ApiContract, "server", "Server information"))

  val Bindings = Field(
    OperationBindingsModel,
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "Bindings for this operation")
  )

  val Provides = Field(EntityProviderModel,
                       ApiFederation + "provides",
                       ModelDoc(ModelVocabularies.ApiFederation, "provides", "External property"))

  override val key: Field = Method

  override val `type`: List[ValueType] = ApiContract + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(
    Method,
    Name,
    Description,
    Deprecated,
    Summary,
    Documentation,
    Schemes,
    Accepts,
    ContentType,
    Request,
    Responses,
    Security,
    Tags,
    Callbacks,
    Servers,
    Bindings,
    IsAbstract,
    OperationId,
    Provides
  ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = Operation()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Operation",
    "Action that can be executed using a particular HTTP invocation"
  )
}
