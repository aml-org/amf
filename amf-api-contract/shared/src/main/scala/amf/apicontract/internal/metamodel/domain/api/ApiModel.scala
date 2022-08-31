package amf.apicontract.internal.metamodel.domain.api

import amf.apicontract.internal.metamodel.domain._
import amf.apicontract.internal.metamodel.domain.common.VersionField
import amf.apicontract.internal.metamodel.domain.security.SecurityRequirementModel
import amf.core.client.scala.vocabulary.Namespace._
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.internal.domain.metamodel.CreativeWorkModel

/** Web Api metamodel
  */
trait ApiModel extends DomainElementModel with NameFieldSchema with DescriptionField with TagsModel with VersionField {

  val Servers =
    Field(
      Array(ServerModel),
      ApiContract + "server",
      ModelDoc(ModelVocabularies.ApiContract, "server", "Server information")
    )

  val Accepts = Field(
    Array(Str),
    ApiContract + "accepts",
    ModelDoc(ModelVocabularies.ApiContract, "accepts", "Media-types accepted in a API request")
  )

  val ContentType = Field(
    Array(Str),
    ApiContract + "contentType",
    ModelDoc(ModelVocabularies.ApiContract, "contentType", "Media types returned by a API response")
  )

  val Identifier = Field(
    Str,
    Core + "identifier",
    ModelDoc(
      ModelVocabularies.Core,
      "identifier",
      "The identifier property represents any kind of identifier, such as ISBNs, GTIN codes, UUIDs, etc."
    )
  )

  val Schemes =
    Field(
      Array(Str),
      ApiContract + "scheme",
      ModelDoc(ModelVocabularies.ApiContract, "scheme", "URI scheme for the API protocol")
    )

  val TermsOfService = Field(
    Str,
    Core + "termsOfService",
    ModelDoc(ModelVocabularies.Core, "termsOfService", "Terms and conditions when using the API")
  )

  val Provider = Field(
    OrganizationModel,
    Core + "provider",
    ModelDoc(ModelVocabularies.Core, "provider", "Organization providing some kind of asset or service")
  )

  val License =
    Field(LicenseModel, Core + "license", ModelDoc(ModelVocabularies.Core, "license", "License for the API"))

  val Documentations = Field(
    Array(CreativeWorkModel),
    Core + "documentation",
    ModelDoc(ModelVocabularies.Core, "documentation", "Documentation associated to the API")
  )

  val EndPoints = Field(
    Array(EndPointModel),
    ApiContract + "endpoint",
    ModelDoc(ModelVocabularies.ApiContract, "endpoint", "End points defined in the API")
  )

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Textual indication of the kind of security scheme used")
  )

  override val `type`: List[ValueType] =
    ApiContract + "API" :: Document + "RootDomainElement" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Description,
      Identifier,
      Servers,
      Accepts,
      ContentType,
      Schemes,
      Version,
      TermsOfService,
      Provider,
      License,
      Documentations,
      EndPoints,
      Security,
      Tags
    ) ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "API",
    "Top level element describing any kind of API"
  )
}

object BaseApiModel extends ApiModel {
  override def modelInstance = throw new Exception("ApiModel is an abstract class")
}
