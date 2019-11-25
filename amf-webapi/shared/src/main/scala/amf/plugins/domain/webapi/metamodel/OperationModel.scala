package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.{ApiContract, Core}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel.common.DocumentationField
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.models.Operation

/**
  * Operation meta model.
  */
object OperationModel
    extends DomainElementModel
    with KeyField
    with OptionalField
    with NameFieldSchema
    with DescriptionField
    with DocumentationField {

  val Method = Field(Str,
                     ApiContract + "method",
                     ModelDoc(ModelVocabularies.ApiContract, "method", "HTTP method required to invoke the operation"))

  val Deprecated = Field(Bool,
                         Core + "deprecated",
                         ModelDoc(ModelVocabularies.Core, "deprecated", "Marks the operation as deprecated"))

  val Summary = Field(
    Str,
    ApiContract + "guiSummary",
    ModelDoc(ModelVocabularies.ApiContract,
             "gui summary",
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
                          ModelDoc(ModelVocabularies.Core, "media type", "Media types returned by a API response"))

  val Request = Field(
    RequestModel,
    ApiContract + "expects",
    ModelDoc(ModelVocabularies.ApiContract, "expects", "Request information required by the operation"))

  val Responses = Field(Array(ResponseModel),
                        ApiContract + "returns",
                        ModelDoc(ModelVocabularies.ApiContract, "returns", "Response data returned by the operation"))

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "security schemes applied to an element in the API spec")
  )

  val Tags =
    Field(Array(Str),
          ApiContract + "tag",
          ModelDoc(ModelVocabularies.ApiContract, "tag", "Additionally custom tagged information"))

  val Callbacks = Field(Array(CallbackModel),
                        ApiContract + "callback",
                        ModelDoc(ModelVocabularies.ApiContract, "callback", "associated callbacks"))

  val Servers =
    Field(Array(ServerModel),
          ApiContract + "server",
          ModelDoc(ModelVocabularies.ApiContract, "server", "server information"))

  override val key: Field = Method

  override val `type`: List[ValueType] = ApiContract + "Operation" :: DomainElementModel.`type`

  override val fields: List[Field] = List(Method,
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
                                          Servers) ++ DomainElementModel.fields

  override def modelInstance = Operation()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Operation",
    "Action that can be executed using a particular HTTP invocation"
  )
}
