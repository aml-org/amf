package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.internal.metamodel.domain.bindings.ChannelBindingsModel
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel
import amf.core.client.scala.vocabulary.Namespace.{ApiBinding, ApiContract, Core}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

/** EndPoint metaModel
  *
  * EndPoint in the API holding a number of executable operations
  */
object EndPointModel extends DomainElementModel with KeyField with NameFieldSchema with DescriptionField {

  val Path =
    Field(Str, ApiContract + "path", ModelDoc(ModelVocabularies.ApiContract, "path", "Path template for an endpoint"))

  val Summary = Field(
    Str,
    Core + "summary",
    ModelDoc(ModelVocabularies.Core, "summary", "Human readable short description of the endpoint")
  )

  val Operations = Field(
    Array(OperationModel),
    ApiContract + "supportedOperation",
    ModelDoc(ModelVocabularies.ApiContract, "supportedOperation", "Operations supported by an endpoint")
  )

  val Parameters = Field(
    Array(ParameterModel),
    ApiContract + "parameter",
    ModelDoc(ModelVocabularies.ApiContract, "parameter", "Additional data required or returned by an operation")
  )

  val Payloads = Field(
    Array(PayloadModel),
    ApiContract + "payload",
    ModelDoc(ModelVocabularies.ApiContract, "payload", "Main payload data required or returned by an operation")
  )

  val Servers = Field(
    Array(ServerModel),
    ApiContract + "server",
    ModelDoc(
      ModelVocabularies.ApiContract,
      "servers",
      "Specific information about the server where the endpoint is accessible"
    )
  )

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Textual indication of the kind of security scheme used")
  )

  val Bindings = Field(
    ChannelBindingsModel,
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "Bindings for this endpoint")
  )

  override val key: Field = Path

  override val `type`: List[ValueType] = ApiContract + "EndPoint" :: DomainElementModel.`type`

  override val fields: List[Field] =
    List(
      Path,
      Name,
      Summary,
      Description,
      Operations,
      Parameters,
      Payloads,
      Servers,
      Security,
      Bindings
    ) ++ DomainElementModel.fields

  override def modelInstance = EndPoint()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "EndPoint",
    "EndPoint in the API holding a number of executable operations"
  )
}
