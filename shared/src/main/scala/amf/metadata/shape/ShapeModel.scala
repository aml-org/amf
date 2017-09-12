package amf.metadata.shape

import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.CreativeWorkModel
import amf.metadata.{Field, Obj}
import amf.vocabulary.Namespace.{Schema, Shacl, Shapes}
import amf.vocabulary.ValueType

trait ShapeModel extends Obj {

  val Name = Field(Str, Shacl + "name")

  val DisplayName = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(Str, Shacl + "defaultValue")

  val Values = Field(Array(Str), Shacl + "in")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val XMLSerialization = Field(XMLSerializerModel, Shapes + "xmlSerialization")
}

object ShapeModel extends ShapeModel {

  override val fields: List[Field] =
    List(Name, DisplayName, Description, Default, Values, Documentation, XMLSerialization)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")
}
