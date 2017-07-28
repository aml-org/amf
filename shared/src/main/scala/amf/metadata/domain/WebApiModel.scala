package amf.metadata.domain

import amf.metadata.Field
import amf.metadata.Type.{Array, Str}
import amf.vocabulary.Namespace._
import amf.vocabulary.ValueType

/**
  * Web Api metamodel
  */
object WebApiModel extends DomainElementModel {

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Host = Field(Str, Http + "host")

  val Schemes = Field(Array(Str), Http + "schemes")

  val BasePath = Field(Str, Http + "basePath")

  val Accepts = Field(Str, Http + "accepts")

  val ContentType = Field(Str, Http + "contentType")

  val Version = Field(Str, Schema + "version")

  val TermsOfService = Field(Str, Schema + "termsOfService")

  val Provider = Field(OrganizationModel, Schema + "provider")

  val License = Field(LicenseModel, Schema + "license")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val EndPoints = Field(Array(EndPointModel), Http + "endpoint")

  val BaseUriParameters = Field(Array(ParameterModel), Http + "parameter")

  override val `type`: List[ValueType] = Schema + "WebAPI" :: DomainElementModel.`type`

  override val fields
    : List[Field] = Name :: Description :: Host :: Schemes :: BasePath :: Accepts :: ContentType :: Version :: TermsOfService :: Provider :: License :: Documentation :: EndPoints :: BaseUriParameters :: DomainElementModel.fields
}
