package amf.metadata.shape

import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.CreativeWorkModel
import amf.metadata.{Field, Obj}
import amf.vocabulary.Namespace.{Schema, Shacl}
import amf.vocabulary.ValueType

trait ShapeModel extends Obj {

  val Name = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(Str, Shacl + "defaultValue")

  val In = Field(Array(Str), Shacl + "in")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")
}

object ShapeModel extends ShapeModel {

  override val fields: List[Field] = List(Name, Description, Default, In, Documentation)

  override val `type`: List[ValueType] = List(Shacl + "Shape")
}
