package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.vocabulary.Namespace.{ApiContract, Core, ApiBinding}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.webapi.metamodel.bindings.ServerBindingModel
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.models.Server

/**
  * Server meta model
  */
object ServerModel extends DomainElementModel with NameFieldSchema with DescriptionField {
  val Url = Field(Str,
                  Core + "urlTemplate",
                  ModelDoc(ModelVocabularies.Core, "urlTemplate", "URL (potentially a template) for the server"))

  val Variables = Field(Array(ParameterModel),
                        ApiContract + "variable",
                        ModelDoc(ModelVocabularies.ApiContract, "variable", "Variables in the URL for the server"))

  val Protocol = Field(
    Str,
    ApiContract + "protocol",
    ModelDoc(ModelVocabularies.ApiContract, "protocol", "The protocol this URL supports for connection"))

  val ProtocolVersion = Field(
    Str,
    ApiContract + "protocolVersion",
    ModelDoc(ModelVocabularies.ApiContract, "protocolVersion", "The version of the protocol used for connection"))

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Textual indication of the kind of security scheme used")
  )

  val Bindings = Field(
    Array(ServerBindingModel),
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "Bindings for this server")
  )

  override val `type`: List[ValueType] = ApiContract + "Server" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Url,
      Description,
      Variables,
      Protocol,
      ProtocolVersion,
      Security,
      Bindings
    ) ++ DomainElementModel.fields

  override def modelInstance = Server()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Server",
    "Information about the network accessible locations where the API is available"
  )
}
