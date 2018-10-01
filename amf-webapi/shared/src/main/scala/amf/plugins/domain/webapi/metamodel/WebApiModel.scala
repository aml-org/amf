package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
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

  val Servers = Field(Array(ServerModel), Http + "server")

  val Accepts = Field(Array(Str), Http + "accepts")

  val ContentType = Field(Array(Str), Http + "contentType")

  val Schemes = Field(Array(Str), Http + "scheme")

  val Version = Field(Str, Schema + "version")

  val TermsOfService = Field(Str, Schema + "termsOfService")

  val Provider = Field(OrganizationModel, Schema + "provider")

  val License = Field(LicenseModel, Schema + "license")

  val Documentations = Field(Array(CreativeWorkModel), Schema + "documentation")

  val EndPoints = Field(Array(EndPointModel), Http + "endpoint")

  val Security = Field(Array(ParametrizedSecuritySchemeModel), Namespace.Security + "security")

  val Tags = Field(Array(TagModel), Http + "tag")

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
}
