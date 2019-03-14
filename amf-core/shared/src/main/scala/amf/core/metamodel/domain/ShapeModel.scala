package amf.core.metamodel.domain

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Iri, SortedArray, Str}
import amf.core.metamodel.domain.common.{DescriptionField, DisplayNameField}
import amf.core.metamodel.domain.extensions.{PropertyShapeModel, ShapeExtensionModel}
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.{Schema, Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Base class for all shapes. Shapes are Domain Entities that define constraints over parts of a data graph.
  * They can be used to define and enforce schemas for the data graph information through SHACL.
  * Shapes can be recursive and inherit from other shapes.
  */
trait ShapeModel extends DomainElementModel with LinkableElementModel with KeyField with DescriptionField {

  val Name = Field(Str, Shacl + "name", ModelDoc(ExternalModelVocabularies.Shacl, "name", "Name for a data shape"))

  val DisplayName =
    Field(Str, Schema + "name", ModelDoc(ExternalModelVocabularies.SchemaOrg, "name", "Name for a data shape"))

  val Default = Field(
    DataNodeModel,
    Shacl + "defaultValue",
    ModelDoc(ExternalModelVocabularies.Shacl, "default value", "Default value parsed for a data shape property"))

  // TODO: change namespace
  val DefaultValueString = Field(
    Str,
    Shacl + "defaultValueStr",
    ModelDoc(ExternalModelVocabularies.Shacl,
             "default value String",
             "Textual representation of the parsed default value for the shape property")
  )

  val Values = Field(
    SortedArray(DataNodeModel),
    Shacl + "in",
    ModelDoc(ExternalModelVocabularies.Shacl, "in", "Enumeration of possible values for a data shape property"))

  val Closure = Field(
    Array(Iri),
    Shapes + "closure",
    ModelDoc(ModelVocabularies.Shapes,
             "inheritance closure",
             "Transitive closure of data shapes this particular shape inherits structure from")
  )

  /**
    * Inheritance relationship between shapes. Introduces the idea that the constraints defined by this shape are a specialization of the constraints of the base shapes.
    * Graphs validating this shape should also validate all the constraints for the base shapes
    */
  val Inherits = Field(
    Array(ShapeModel),
    Shapes + "inherits",
    ModelDoc(ModelVocabularies.Shapes, "inherits", "Relationship of inheritance between data shapes"))

  // Logical constraints:

  val Or = Field(Array(ShapeModel),
                 Shacl + "or",
                 ModelDoc(ExternalModelVocabularies.Shacl, "or", "Logical or composition of data shapes"))

  val And = Field(Array(ShapeModel),
                  Shacl + "and",
                  ModelDoc(ExternalModelVocabularies.Shacl, "and", "Logical and composition of data shapes"))

  val Xone = Field(
    Array(ShapeModel),
    Shacl + "xone",
    ModelDoc(ExternalModelVocabularies.Shacl, "exclusive or", "Logical exclusive or composition of data shapes"))

  val Not = Field(ShapeModel,
                  Shacl + "not",
                  ModelDoc(ExternalModelVocabularies.Shacl, "not", "Logical not composition of data shapes"))

  override val key: Field = Name

  // RAML user-defined facets: definitions and values
  lazy val CustomShapePropertyDefinitions = Field(
    Array(PropertyShapeModel),
    Shapes + "customShapePropertyDefinitions",
    ModelDoc(ModelVocabularies.Shapes,
             "custom shape property definitions",
             "Custom constraint definitions added over a data shape")
  )
  lazy val CustomShapeProperties = Field(
    Array(ShapeExtensionModel),
    Shapes + "customShapeProperties",
    ModelDoc(ModelVocabularies.Shapes, "custom shape properties", "Custom constraint values for a data shape")
  )
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
                                                                         Xone,
                                                                         Closure)

  override val `type`: List[ValueType] = List(Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = throw new Exception("Shape is abstract and it cannot be instantiated by default")

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Shape",
    "Base class for all shapes. Shapes are Domain Entities that define constraints over parts of a data graph.\nThey can be used to define and enforce schemas for the data graph information through SHACL.\nShapes can be recursive and inherit from other shapes."
  )
}
