package amf.apicontract.internal.metamodel.domain

import amf.apicontract.client.scala.model.domain.Server
import amf.apicontract.internal.metamodel.domain.bindings.ServerBindingsModel
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel
import amf.core.client.scala.vocabulary.Namespace.{ApiBinding, ApiContract, Core}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

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
    ServerBindingsModel,
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
