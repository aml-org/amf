package amf.metadata.model

import amf.metadata.Type.Str
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Schema

/**
  * Organization metamodel
  */
object OrganizationModel extends Type {

  val Url = Field(Str, Schema, "url")

  val Name = Field(Str, Schema, "name")

  val Email = Field(Str, Schema, "email")
}
