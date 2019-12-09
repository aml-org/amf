package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Int, SortedArray, Str}
import amf.core.metamodel.domain._
import amf.plugins.domain.shapes.models.{ArrayShape, MatrixShape, TupleShape}
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Array shape metamodel
  */
/**
  * Common fields to all arrays, matrix and tuples
  */
class DataArrangementShape extends AnyShapeModel {
  val Items = Field(ShapeModel,
                    Shapes + "items",
                    ModelDoc(ModelVocabularies.Shapes, "items", "Shapes inside the data arrangement"))

  val Contains = Field(ShapeModel,
                       Shapes + "contains",
                       ModelDoc(ModelVocabularies.Shapes, "contains", "One of the shapes in the data arrangement"))

  val MinItems = Field(Int,
                       Shacl + "minCount",
                       ModelDoc(ExternalModelVocabularies.Shacl, "min count", "Minimum items count constraint"))

  val MaxItems = Field(Int,
                       Shacl + "maxCount",
                       ModelDoc(ExternalModelVocabularies.Shacl, "max count", "Maximum items count constraint"))

  val UniqueItems =
    Field(Bool, Shapes + "uniqueItems", ModelDoc(ModelVocabularies.Shapes, "uinque items", "Unique items constraint"))

  val CollectionFormat = Field(
    Str,
    Shapes + "collectionFormat",
    ModelDoc(ModelVocabularies.Shapes, "collection format", "Input collection format information"))

  val specificFields               = List(Items, Contains, MinItems, MaxItems, UniqueItems, CollectionFormat)
  override val fields: List[Field] = specificFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Data Arrangement Shape",
    "Base shape class for any data shape that contains a nested collection of data shapes"
  )
}

object ArrayShapeModel extends DataArrangementShape with DomainElementModel {

  override val `type`: List[ValueType] = List(Shapes + "ArrayShape") ++ AnyShapeModel.`type`

  override def modelInstance = ArrayShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Array Shape",
    "Shape that contains a nested collection of data shapes"
  )
}

object MatrixShapeModel extends DataArrangementShape with DomainElementModel {
  override val `type`: List[ValueType] = List(Shapes + "MatrixShape", Shapes + "ArrayShape") ++ AnyShapeModel.`type`
  override def modelInstance           = MatrixShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Matrix Shape",
    "Data shape containing nested multi-dimensional collection shapes"
  )
}

object TupleShapeModel extends DataArrangementShape with DomainElementModel {
  val ClosedItems = Field(
    Bool,
    Shapes + "closedItems",
    ModelDoc(ModelVocabularies.Shapes, "closed items", "Constraint limiting additional shapes in the collection"))

  val AdditionalItemsSchema = Field(ShapeModel,
                                    Shapes + "additionalItemsSchema",
                                    ModelDoc(ModelVocabularies.Shapes, "additional items schema", ""))

  val TupleItems = Field(SortedArray(ShapeModel),
                         Shapes + "items",
                         ModelDoc(ModelVocabularies.Shapes, "items", "Shapes contained in the Tuple Shape"))
  override val `type`: List[ValueType] = List(Shapes + "TupleShape", Shapes + "ArrayShape") ++ AnyShapeModel.`type`
  override def modelInstance           = TupleShape()

  override val fields: List[Field] = List(TupleItems,
                                          MinItems,
                                          MaxItems,
                                          UniqueItems,
                                          ClosedItems,
                                          AdditionalItemsSchema,
                                          CollectionFormat) ++ AnyShapeModel.fields ++ DomainElementModel.fields
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Tuple Shape",
    "Data shape containing a multi-valued collection of shapes"
  )
}
