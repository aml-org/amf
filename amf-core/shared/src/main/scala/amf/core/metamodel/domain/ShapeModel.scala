package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, SortedArray, Str}
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.{Schema, Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Base class for all shapes. Shapes are Domain Entities that define constraints over parts of a data graph.
  * They can be used to define and enforce schemas for the data graph information through SHACL.
  * Shapes can be recursive and inherit from other shapes.
  */
trait ShapeModel extends DomainElementModel with LinkableElementModel with KeyField {

  val Name = Field(Str, Shacl + "name")

  val DisplayName = Field(Str, Schema + "name")

  val Description = Field(Str, Schema + "description")

  val Default = Field(DataNodeModel, Shacl + "defaultValue")

  val DefaultValueString = Field(Str, Shacl + "defaultValueStr")

  val Values = Field(SortedArray(DataNodeModel), Shacl + "in")

  /**
    * Inheritance relationship between shapes. Introduces the idea that the constraints defined by this shape are a specialization of the constraints of the base shapes.
    * Graphs validating this shape should also validate all the constraints for the base shapes
    */
  val Inherits = Field(Array(ShapeModel), Shapes + "inherits")

  // Logical constraints:

  val Or = Field(Array(ShapeModel), Shacl + "or")

  val And = Field(Array(ShapeModel), Shacl + "and")

  val Xone = Field(Array(ShapeModel), Shacl + "xone")

  val Not = Field(ShapeModel, Shacl + "not")

  override val key: Field = Name

  // RAML user-defined facets: definitions and values
  lazy val CustomShapePropertyDefinitions = Field(Array(PropertyShapeModel), Shapes + "customShapePropertyDefinitions")
  lazy val CustomShapeProperties          = Field(Array(ShapeExtensionModel), Shapes + "customShapeProperties")
  //

}

object ShapeModel extends ShapeModel {

  override val fields: List[Field] = LinkableElementModel.fields ++ List(Name,
                                                                         DisplayName,
                                                                         Description,
                                                                         Default,
                                                                         Values,
                                                                         Inherits,
                                                                         DefaultValueString,
                                                                         Not,
                                                                         And,
                                                                         Or,
                                                                         Xone)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = throw new Exception("Shape is abstract and it cannot be instantiated by default")
}
