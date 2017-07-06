package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type.{Array, Str}

/**
  * WebApi
  */
object WebApiModel extends Type {

  val Name = Field(Str, "name")

  val Description = Field(Str, "description")

  val Host = Field(Str, "host")

  val Schemes = Field(Array(Str), "schemes")

  val BasePath = Field(Str, "basepath")

  val Accepts = Field(Str, "accepts")

  val ContentType = Field(Str, "contenttype")

  val Version = Field(Str, "version")

  val TermsOfService = Field(Str, "termsofservice")

  val Provider = Field(OrganizationModel, "provider")

  val License = Field(LicenseModel, "license")

  val Documentation = Field(CreativeWorkModel, "documentation")

  val EndPoints = Field(Array(EndPointModel), "endpoints")
}
