package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Int, Iri, Str}
import amf.core.metamodel.domain._
import amf.core.model.domain.extensions.PropertyShape
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Property shape metamodel
  *
  * Model for SHACL PropertyShapes
  */
object PropertyShapeModel extends ShapeModel {

  val Path =
    Field(Iri, Shacl + "path", ModelDoc(ExternalModelVocabularies.Shacl, "path", "Path to the constrained property"))

  val Range =
    Field(ShapeModel, Shapes + "range", ModelDoc(ModelVocabularies.Shapes, "range", "Range property constraint"))

  val MinCount = Field(Int,
                       Shacl + "minCount",
                       ModelDoc(ExternalModelVocabularies.Shacl, "min. count", "Minimum count property constraint"))

  val MaxCount = Field(Int,
                       Shacl + "maxCount",
                       ModelDoc(ExternalModelVocabularies.Shacl, "max. count", "Maximum count property constraint"))

  val ReadOnly =
    Field(Bool, Shapes + "readOnly", ModelDoc(ModelVocabularies.Shapes, "read only", "Read only property constraint"))

  val WriteOnly = Field(Bool,
                        Shapes + "writeOnly",
                        ModelDoc(ModelVocabularies.Shapes, "write only", "Write only property constraint"))

  val Deprecated = Field(
    Bool,
    Shapes + "deprecated",
    ModelDoc(ModelVocabularies.Shapes, "deprecated", "Deprecated annotation for a property constraint"))

  val PatternName = Field(Str,
                          Shapes + "patternName",
                          ModelDoc(ModelVocabularies.Shapes, "pattern name", "Patterned property constraint"))

  override val `type`: List[ValueType] = List(Shacl + "PropertyShape") ++ ShapeModel.`type`

  override def fields: List[Field] =
    List(Path, Range, MinCount, MaxCount, ReadOnly, PatternName) ++ ShapeModel.fields ++ DomainElementModel.fields

  override def modelInstance = PropertyShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Property Shape",
    "Constraint over a property in a data shape."
  )
}
