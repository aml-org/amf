package amf.metadata.domain

import amf.metadata.Type.Str
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Schema

/**
  * Organization metamodel
  */
object OrganizationModel extends DomainElementModel {

  val Url = Field(Str, Schema, "url")

  val Name = Field(Str, Schema, "name")

  val Email = Field(Str, Schema, "email")
}
