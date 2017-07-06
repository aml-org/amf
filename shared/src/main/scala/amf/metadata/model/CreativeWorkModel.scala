package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type.Str

/**
  * Creative work
  */
object CreativeWorkModel extends Type {

  val Url = Field(Str, "url")

  val Description = Field(Str, "description")
}
