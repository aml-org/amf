package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type._

/**
  * Operation
  */
object OperationModel extends Type {

  val Method = Field(Enum, "method")

  val Name = Field(Str, "name")

  val Description = Field(Str, "description")

  val Deprecated = Field(Bool, "deprecated")

  val Summary = Field(Bool, "summary")

  val Documentation = Field(CreativeWorkModel, "documentation")

  val Schemes = Field(Array(Str), "schemes")

}
