package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type.Str

/**
  * Organization
  */
object OrganizationModel extends Type {

  val Url = Field(Str, "url")

  val Name = Field(Str, "name")

  val Email = Field(Str, "email")
}
