package amf.metadata.model

import amf.metadata.Type.Str
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.Schema

/**
  * Creative work metamodel
  */
object CreativeWorkModel extends Type {

  val Url = Field(Str, Schema, "url")

  val Description = Field(Str, Schema, "description")
}
