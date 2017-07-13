package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type.{Array, Str}
import amf.vocabulary.Namespace._

/**
  * WebApi metamodel
  */
object WebApiModel extends Type {

  val Name = Field(Str, Schema, "name")

  val Description = Field(Str, Schema, "description")

  val Host = Field(Str, Http, "host")

  val Schemes = Field(Array(Str), Http, "schemes")

  val BasePath = Field(Str, Http, "basePath")

  val Accepts = Field(Str, Http, "accepts")

  val ContentType = Field(Str, Http, "contentType")

  val Version = Field(Str, Schema, "version")

  val TermsOfService = Field(Str, Schema, "termsOfService")

  val Provider = Field(OrganizationModel, Schema, "provider")

  val License = Field(LicenseModel, Schema, "license")

  val Documentation = Field(CreativeWorkModel, Schema, "documentation")

  val EndPoints = Field(Array(EndPointModel), Http, "endpoint")
}
