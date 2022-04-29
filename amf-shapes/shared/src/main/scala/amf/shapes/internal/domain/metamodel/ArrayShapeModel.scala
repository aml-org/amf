package amf.shapes.internal.domain.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Bool, Int, SortedArray, Str}
import amf.core.internal.metamodel.domain._
import amf.core.client.scala.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.model.domain.{ArrayShape, MatrixShape, TupleShape}

/** Array shape metamodel
  */
/** Common fields to all arrays, matrix and tuples
  */
class DataArrangementShape extends AnyShapeModel {
  val Items = Field(
    ShapeModel,
    Shapes + "items",
    ModelDoc(ModelVocabularies.Shapes, "items", "Shapes inside the data arrangement")
  )

  val MinItems = Field(
    Int,
    Shacl + "minCount",
    ModelDoc(ExternalModelVocabularies.Shacl, "minCount", "Minimum items count constraint")
  )

  val MaxItems = Field(
    Int,
    Shacl + "maxCount",
    ModelDoc(ExternalModelVocabularies.Shacl, "maxCount", "Maximum items count constraint")
  )

  val UniqueItems =
    Field(Bool, Shapes + "uniqueItems", ModelDoc(ModelVocabularies.Shapes, "uniqueItems", "Unique items constraint"))

  // TODO: Should be sh:qualifiedValue. Not changing it for backwards compatibility
  val Contains = Field(
    ShapeModel,
    Shapes + "contains",
    ModelDoc(ModelVocabularies.Shapes, "contains", "One of the shapes in the data arrangement")
  )

  val MinContains =
    Field(
      Int,
      Shacl + "qualifiedMinCount",
      ModelDoc(ExternalModelVocabularies.Shacl, "qualifiedMinCount", "Minimum number of value nodes constraint")
    )

  val MaxContains =
    Field(
      Int,
      Shacl + "qualifiedMaxCount",
      ModelDoc(ExternalModelVocabularies.Shacl, "qualifiedMaxCount", "Maximum number of value nodes constraint")
    )

  val CollectionFormat = Field(
    Str,
    Shapes + "collectionFormat",
    ModelDoc(ModelVocabularies.Shapes, "collectionFormat", "Input collection format information")
  )

  val UnevaluatedItems = Field(
    Bool,
    Shapes + "unevaluatedItems",
    ModelDoc(
      ModelVocabularies.Shapes,
      "unevaluatedItems",
      "Accepts that items may not be evaluated in schema validation"
    )
  )

  val UnevaluatedItemsSchema = Field(
    ShapeModel,
    Shapes + "unevaluatedItemsSchema",
    ModelDoc(ModelVocabularies.Shapes, "unevaluatedItemsSchema", "Items that may not be evaluated in schema validation")
  )

  val specificFields = List(
    Items,
    Contains,
    MinItems,
    MaxItems,
    UniqueItems,
    CollectionFormat,
    UnevaluatedItems,
    UnevaluatedItemsSchema,
    MinContains,
    MaxContains
  )
  override val fields: List[Field] = specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "DataArrangementShape",
    "Base shape class for any data shape that contains a nested collection of data shapes"
  )
}

object ArrayShapeModel extends DataArrangementShape with DomainElementModel {

  override val `type`: List[ValueType] = List(Shapes + "ArrayShape") ++ AnyShapeModel.`type`

  override def modelInstance = ArrayShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ArrayShape",
    "Shape that contains a nested collection of data shapes"
  )
}

object MatrixShapeModel extends DataArrangementShape with DomainElementModel {
  override val `type`: List[ValueType] = List(Shapes + "MatrixShape", Shapes + "ArrayShape") ++ AnyShapeModel.`type`
  override def modelInstance           = MatrixShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "MatrixShape",
    "Data shape containing nested multi-dimensional collection shapes"
  )
}

object TupleShapeModel extends DataArrangementShape with DomainElementModel {
  val ClosedItems = Field(
    Bool,
    Shapes + "closedItems",
    ModelDoc(ModelVocabularies.Shapes, "closedItems", "Constraint limiting additional shapes in the collection")
  )

  val AdditionalItemsSchema = Field(
    ShapeModel,
    Shapes + "additionalItemsSchema",
    ModelDoc(
      ModelVocabularies.Shapes,
      "additionalItemsSchema",
      "Controls whether it’s valid to have additional items in the array beyond what is defined"
    )
  )

  val TupleItems = Field(
    SortedArray(ShapeModel),
    Shapes + "items",
    ModelDoc(ModelVocabularies.Shapes, "items", "Shapes contained in the Tuple Shape")
  )
  override val `type`: List[ValueType] = List(Shapes + "TupleShape", Shapes + "ArrayShape") ++ AnyShapeModel.`type`
  override def modelInstance           = TupleShape()

  override val fields: List[Field] = List(
    TupleItems,
    MinItems,
    MaxItems,
    UniqueItems,
    ClosedItems,
    AdditionalItemsSchema,
    CollectionFormat
  ) ++ AnyShapeModel.fields ++ DomainElementModel.fields
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "TupleShape",
    "Data shape containing a multi-valued collection of shapes"
  )
}
