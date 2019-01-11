package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.KeyField
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.models.EndPoint
import amf.core.vocabulary.Namespace.{Http, Hydra, Schema}
import amf.core.vocabulary.{Namespace, ValueType}

/**
  * EndPoint metaModel
  *
  * EndPoint in the API holding a number of executable operations
  */
object EndPointModel extends DomainElementModel with KeyField with NameFieldSchema with DescriptionField {

  val Path = Field(Str, Http + "path", ModelDoc(ModelVocabularies.Http, "path", "Path template for an endpoint"))

  val Summary = Field(
    Str,
    Schema + "summary",
    ModelDoc(ExternalModelVocabularies.SchemaOrg,
             "summary",
             "Human readable description of the endpoint",
             Seq((Namespace.Schema + "description").iri()))
  )

  val Operations = Field(
    Array(OperationModel),
    Hydra + "supportedOperation",
    ModelDoc(ExternalModelVocabularies.Hydra, "supported operation", "Operations supported by an endpoint")
  )

  val Parameters = Field(
    Array(ParameterModel),
    Http + "parameter",
    ModelDoc(ModelVocabularies.Http, "parameter", "Additional data required or returned by an operation"))

  val Payloads = Field(
    Array(PayloadModel),
    Http + "payload",
    ModelDoc(ModelVocabularies.Http, "payload", "Main payload data required or returned by an operation"))

  val Servers = Field(Array(ServerModel),
                      Http + "server",
                      ModelDoc(ModelVocabularies.Http,
                               "servers",
                               "Specific information about the server where the endpoint is accessible"))

  val Security = Field(
    Array(ParametrizedSecuritySchemeModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Security information associated to the endpoint")
  )

  override val key: Field = Path

  override val `type`: List[ValueType] = Http + "EndPoint" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(Path, Name, Summary, Description, Operations, Parameters, Payloads, Servers, Security) ++ DomainElementModel.fields

  override def modelInstance = EndPoint()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Http,
    "EndPoint",
    "EndPoint in the API holding a number of executable operations"
  )
}
