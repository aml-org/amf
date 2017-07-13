package amf.metadata.domain

import amf.metadata.Type._
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.{Http, Hydra, Schema}

/**
  * EndPoint metamodel
  */
object EndPointModel extends Type {

  val Path = Field(RegExp, Http, "path")

  val Name = Field(Str, Schema, "name")

  val Description = Field(Str, Schema, "description")

  val Operations = Field(Array(OperationModel), Hydra, "supportedOperation")
}
