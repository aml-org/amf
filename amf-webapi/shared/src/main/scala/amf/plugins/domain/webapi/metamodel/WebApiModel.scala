package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel
import amf.plugins.domain.webapi.metamodel.security.SecurityRequirementModel
import amf.plugins.domain.webapi.models.WebApi

/**
  * Web Api metamodel
  */
object WebApiModel extends DomainElementModel with NameFieldSchema with DescriptionField {

  val Servers =
    Field(Array(ServerModel),
          ApiContract + "server",
          ModelDoc(ModelVocabularies.ApiContract, "server", "server information"))

  val Accepts = Field(Array(Str),
                      ApiContract + "accepts",
                      ModelDoc(ModelVocabularies.ApiContract, "accepts", "Media-types accepted in a API request"))

  val ContentType = Field(
    Array(Str),
    ApiContract + "contentType",
    ModelDoc(ModelVocabularies.ApiContract, "content type", "Media types returned by a API response"))

  val Schemes =
    Field(Array(Str),
          ApiContract + "scheme",
          ModelDoc(ModelVocabularies.ApiContract, "scheme", "URI scheme for the API protocol"))

  val Version =
    Field(Str, Core + "version", ModelDoc(ModelVocabularies.Core, "version", "Version of the API"))

  val TermsOfService = Field(
    Str,
    Core + "termsOfService",
    ModelDoc(ModelVocabularies.Core, "terms of service", "Terms and conditions when using the API"))

  val Provider = Field(
    OrganizationModel,
    Core + "provider",
    ModelDoc(ModelVocabularies.Core, "provider", "Organization providing some kind of asset or service"))

  val License =
    Field(LicenseModel, Core + "license", ModelDoc(ModelVocabularies.Core, "license", "License for the API"))

  val Documentations = Field(Array(CreativeWorkModel),
                             Core + "documentation",
                             ModelDoc(ModelVocabularies.Core, "documentation", "Documentation associated to the API"))

  val EndPoints = Field(Array(EndPointModel),
                        ApiContract + "endpoint",
                        ModelDoc(ModelVocabularies.ApiContract, "endpoint", "End points defined in the API"))

  val Security = Field(
    Array(SecurityRequirementModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Textual indication of the kind of security scheme used")
  )

  val Tags = Field(Array(TagModel),
                   ApiContract + "tag",
                   ModelDoc(ModelVocabularies.ApiContract, "tag", "Additionally custom tagged information"))

  override val `type`
    : List[ValueType] = ApiContract + "WebAPI" :: Document + "RootDomainElement" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Description,
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

  override def modelInstance = WebApi()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Web API",
    "Top level element describing a HTTP API"
  )
}
