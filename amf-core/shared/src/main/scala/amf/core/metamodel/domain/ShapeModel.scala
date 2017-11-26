package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Bool, SortedArray, Str}
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.{Schema, Shacl, Shapes}
import amf.core.vocabulary.ValueType

trait ShapeModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name = Field(Str, Shacl + "name")

  val DisplayName = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(Str, Shacl + "defaultValue")

  val Values = Field(SortedArray(Str), Shacl + "in")

  val Inherits = Field(Array(ShapeModel), Shapes + "inherits")

  override val key: Field = Name

  // This is just a placeholder for the required shape information that is
  // stored in the model in the MinCount field of the PropertyShape
  // This should not be serialised into the JSON-LD document
  val RequiredShape = Field(Bool, Shapes + "requiredShape", jsonldField = false)

  // RAML user-defined facets: definitions and values
  lazy val CustomShapePropertyDefinitions = Field(Array(PropertyShapeModel), Shapes + "customShapePropertyDefinitions")
  lazy val CustomShapeProperties = Field(Array(ShapeExtensionModel), Shapes + "customShapeProperties")
  //

}

object ShapeModel extends ShapeModel {

  override val fields: List[Field] = LinkableElementModel.fields ++ List(Name,
                                                                         DisplayName,
                                                                         Description,
                                                                         Default,
                                                                         Values,
                                                                         Inherits)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = throw new Exception("Shape is abstract and it cannot be instantiated by default")
}
