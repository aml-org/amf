package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, SortedArray, Str}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel}
import amf.plugins.domain.webapi.metamodel.CreativeWorkModel
import amf.core.vocabulary.Namespace.{Document, Schema, Shacl, Shapes}
import amf.core.vocabulary.ValueType

trait ShapeModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name = Field(Str, Shacl + "name")

  val DisplayName = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(Str, Shacl + "defaultValue")

  val Values = Field(SortedArray(Str), Shacl + "in")

  val Documentation = Field(CreativeWorkModel, Schema + "documentation")

  val XMLSerialization = Field(XMLSerializerModel, Shapes + "xmlSerialization")

  val Inherits = Field(Array(ShapeModel), Shapes + "inherits")

  val Examples = Field(Array(ExampleModel), Document + "examples")

  // RAML user-defined facets: definitions and values
  lazy val CustomShapePropertyDefinitions = Field(Array(PropertyShapeModel), Shapes + "customShapePropertyDefinitions")
  lazy val CustomShapeProperties = Field(Array(ShapeExtensionModel), Shapes + "customShapeProperties")
  //

  override val key: Field = Name

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
                                                                         XMLSerialization,
                                                                         Examples)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = throw new Exception("Shape is abstract and it cannot be instantiated by default")
}
