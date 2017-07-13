package amf.metadata.domain

import amf.metadata.Type.Str
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Schema

/**
  * License metamodel
  */
object LicenseModel extends Type {

  val Url = Field(Str, Schema, "url")

  val Name = Field(Str, Schema, "name")
}
