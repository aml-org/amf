package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type._
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.{ApiContract, Core}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.models.EndPoint

/**
  * EndPoint metaModel
  *
  * EndPoint in the API holding a number of executable operations
  */
object EndPointModel extends DomainElementModel with KeyField with NameFieldSchema with DescriptionField {

  val Path =
    Field(Str, ApiContract + "path", ModelDoc(ModelVocabularies.ApiContract, "path", "Path template for an endpoint"))

  val Summary = Field(Str,
                      Core + "summary",
                      ModelDoc(ModelVocabularies.Core, "summary", "Human readable short description of the endpoint"))

  val Operations = Field(
    Array(OperationModel),
    ApiContract + "supportedOperation",
    ModelDoc(ModelVocabularies.ApiContract, "supported operation", "Operations supported by an endpoint")
  )

  val Parameters = Field(
    Array(ParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.ApiContract, "parameter", "Additional data required or returned by an operation")
  )

  val Payloads = Field(
    Array(PayloadModel),
    ApiContract + "payload",
    ModelDoc(ModelVocabularies.ApiContract, "payload", "Main payload data required or returned by an operation"))

  val Servers = Field(
    Array(ServerModel),
    ApiContract + "server",
    ModelDoc(ModelVocabularies.ApiContract,
             "servers",
             "Specific information about the server where the endpoint is accessible")
  )

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Security information associated to the endpoint")
  )

  override val key: Field = Path

  override val `type`: List[ValueType] = ApiContract + "EndPoint" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(Path, Name, Summary, Description, Operations, Parameters, Payloads, Servers, Security) ++ DomainElementModel.fields

  override def modelInstance = EndPoint()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "EndPoint",
    "EndPoint in the API holding a number of executable operations"
  )
}
