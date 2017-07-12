package amf.metadata.model

import amf.metadata.{Field, Type}
import amf.metadata.Type._

/**
  * EndPoint
  */
object EndPointModel extends Type {

  val Path = Field(RegExp, "path")

  val Name = Field(Str, "name")

  val Description = Field(Str, "description")

  val Operations = Field(Array(OperationModel), "operations")

  val Parent = Field(EndPointModel, "parent")
}
