package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.plugins.domain.webapi.metamodel.security.ParametrizedSecuritySchemeModel
import amf.plugins.domain.webapi.models.WebApi
import amf.core.vocabulary.Namespace._
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.metamodel.CreativeWorkModel

/**
  * Web Api metamodel
  */
object WebApiModel extends DomainElementModel with NameFieldSchema with DescriptionField {

  val Servers =
    Field(Array(ServerModel), Http + "server", ModelDoc(ModelVocabularies.Http, "server", "server information"))

  val Accepts = Field(Array(Str),
                      Http + "accepts",
                      ModelDoc(ModelVocabularies.Http, "accepts", "Media-types accepted in a API request"))

  val ContentType = Field(Array(Str),
                          Http + "contentType",
                          ModelDoc(ModelVocabularies.Http, "content type", "Media types returned by a API response"))

  val Schemes =
    Field(Array(Str), Http + "scheme", ModelDoc(ModelVocabularies.Http, "scheme", "URI scheme for the API protocol"))

  val Version =
    Field(Str, Schema + "version", ModelDoc(ExternalModelVocabularies.SchemaOrg, "version", "Version of the API"))

  val TermsOfService = Field(
    Str,
    Schema + "termsOfService",
    ModelDoc(ExternalModelVocabularies.SchemaOrg, "terms of service", "Terms and conditions when using the API"))

  val Provider = Field(OrganizationModel,
                       Schema + "provider",
                       ModelDoc(ExternalModelVocabularies.SchemaOrg, "provider", "The API provider"))

  val License = Field(LicenseModel,
                      Schema + "license",
                      ModelDoc(ExternalModelVocabularies.SchemaOrg, "license", "License for the API"))

  val Documentations = Field(
    Array(CreativeWorkModel),
    Schema + "documentation",
    ModelDoc(ExternalModelVocabularies.SchemaOrg, "documentation", "Documentation associated to the API"))

  val EndPoints = Field(Array(EndPointModel),
                        Http + "endpoint",
                        ModelDoc(ModelVocabularies.Http, "endpoint", "End points defined in the API"))

  val Security = Field(
    Array(ParametrizedSecuritySchemeModel),
    Namespace.Security + "security",
    ModelDoc(ModelVocabularies.Security, "security", "Textual indication of the kind of security scheme used")
  )

  val Tags = Field(Array(TagModel),
                   Http + "tag",
                   ModelDoc(ModelVocabularies.Http, "tag", "Additionally custom tagged information"))

  override val `type`
    : List[ValueType] = Schema + "WebAPI" :: Document + "RootDomainElement" :: DomainElementModel.`type`

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
    ModelVocabularies.Http,
    "Web API",
    "Top level element describing a HTTP API"
  )
}
