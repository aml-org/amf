package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.security.ParametrizedSecuritySchemeModel
import amf.vocabulary.Namespace._
import amf.vocabulary.{Namespace, ValueType}

/**
  * Web Api metamodel
  */
object WebApiModel extends DomainElementModel {

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Host = Field(Str, Http + "host")

  val Schemes = Field(Array(Str), Http + "scheme")

  val BasePath = Field(Str, Http + "basePath")

  val Accepts = Field(Array(Str), Http + "accepts")

  val ContentType = Field(Array(Str), Http + "contentType")

  val Version = Field(Str, Schema + "version")

  val TermsOfService = Field(Str, Schema + "termsOfService")

  val Provider = Field(OrganizationModel, Schema + "provider")

  val License = Field(LicenseModel, Schema + "license")

  val Documentations = Field(Array(CreativeWorkModel), Schema + "documentation")

  val EndPoints = Field(Array(EndPointModel), Http + "endpoint")

  val BaseUriParameters = Field(Array(ParameterModel), Http + "parameter")

  val Security = Field(Array(ParametrizedSecuritySchemeModel), Namespace.Security + "security")

  override val `type`: List[ValueType] = Schema + "WebAPI" :: DomainElementModel.`type`

  override def fields: List[Field] =
    List(
      Name,
      Description,
      Host,
      Schemes,
      BasePath,
      Accepts,
      ContentType,
      Version,
      TermsOfService,
      Provider,
      License,
      Documentations,
      EndPoints,
      BaseUriParameters,
      Security
    ) ++ DomainElementModel.fields
}
