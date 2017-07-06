package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type.Str

/**
  * License
  */
object LicenseModel extends Type {

  val Url = Field(Str, "url")

  val Name = Field(Str, "name")
}
