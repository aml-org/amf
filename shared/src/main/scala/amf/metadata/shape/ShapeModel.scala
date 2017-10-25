package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Array, SortedArray, Str, Bool}
import amf.metadata.domain.{CreativeWorkModel, DomainElementModel, LinkableElementModel}
import amf.vocabulary.Namespace.{Schema, Shacl, Shapes}
import amf.vocabulary.ValueType

trait ShapeModel extends DomainElementModel with LinkableElementModel {

  val Name = Field(Str, Shacl + "name")

  val DisplayName = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(Str, Shacl + "defaultValue")

  val Values = Field(SortedArray(Str), Shacl + "in")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val XMLSerialization = Field(XMLSerializerModel, Shapes + "xmlSerialization")

  val Inherits = Field(Array(ShapeModel), Shapes + "inherits")

  // This is just a placeholder for the required shape information that is
  // stored in the model in the MinCount field of the PropertyShape
  // This should not be serialised into the JSON-LD document
  val RequiredShape = Field(Bool, Shapes + "requiredShape", jsonldField = false)

}

object ShapeModel extends ShapeModel {

  override val fields: List[Field] = LinkableElementModel.fields ++ List(Name,
                                                                         DisplayName,
                                                                         Description,
                                                                         Default,
                                                                         Values,
                                                                         Documentation,
                                                                         Inherits,
                                                                         XMLSerialization)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")
}
