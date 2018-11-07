package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.{KeyField, OptionalField}
import amf.core.vocabulary.Namespace.{Document, Http, Hydra, Schema}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.shapes.metamodel.common.DocumentationField
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
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

  val Method = Field(
    Str,
    Hydra + "method",
    ModelDoc(ExternalModelVocabularies.Hydra, "method", "HTTP method required to invoke the operation"))

  val Deprecated = Field(Bool,
                         Document + "deprecated",
                         ModelDoc(ModelVocabularies.Http, "deprecated", "Marks the operation as deprecated"))

  val Summary = Field(
    Str,
    Http + "guiSummary",
    ModelDoc(ModelVocabularies.Http,
             "gui summary",
             "Human readable description of the operation",
             Seq((Namespace.Schema + "description").iri()))
  )

  val Schemes =
    Field(Array(Str), Http + "scheme", ModelDoc(ModelVocabularies.Http, "scheme", "URI scheme for the API protocol"))

  val Accepts = Field(Array(Str),
                      Http + "accepts",
                      ModelDoc(ModelVocabularies.Http, "accepts", "Media-types accepted in a API request"))

  val ContentType = Field(Array(Str),
                          Http + "contentType",
                          ModelDoc(ModelVocabularies.Http, "content type", "Media types returned by a API response"))

  val Request = Field(
    RequestModel,
    Hydra + "expects",
    ModelDoc(ExternalModelVocabularies.Hydra, "expects", "Request information required by the operation"))

  val Responses = Field(
    Array(ResponseModel),
    Hydra + "returns",
    ModelDoc(ExternalModelVocabularies.Hydra, "returns", "Response data returned by the operation"))

  val Security = Field(
    Array(ParametrizedSecuritySchemeModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "security schemes applied to an element in the API spec")
  )

  val Tags =
    Field(Array(Str), Http + "tag", ModelDoc(ModelVocabularies.Http, "tag", "Additionally custom tagged information"))

  val Callbacks = Field(Array(CallbackModel),
                        Http + "callback",
                        ModelDoc(ModelVocabularies.Http, "callback", "associated callbacks"))

  val Servers =
    Field(Array(ServerModel), Http + "server", ModelDoc(ModelVocabularies.Http, "server", "server information"))

  override val key: Field = Method

  override val `type`: List[ValueType] = Hydra + "Operation" :: DomainElementModel.`type`

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
    ModelVocabularies.Http,
    "Operation",
    "Action that can be executed using a particular HTTP invocation"
  )
}
