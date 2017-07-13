package amf.metadata.domain

import amf.metadata.Type._
import amf.metadata.{Field, Type}
import amf.vocabulary.Namespace.{Document, Http, Hydra, Schema}

/**
  * Operation metamodel
  */
object OperationModel extends Type {

  val Method = Field(Enum, Hydra, "method")

  val Name = Field(Str, Schema, "name")

  val Description = Field(Str, Schema, "description")

  val Deprecated = Field(Bool, Document, "deprecated")

  val Summary = Field(Bool, Http, "summary")

  val Documentation = Field(CreativeWorkModel, Schema, "documentation")

  val Schemes = Field(Array(Str), Http, "scheme")

}
