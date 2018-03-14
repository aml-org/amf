package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Int, SortedArray, Str}
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
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
  val Items = Field(ShapeModel, Shapes + "items")

  val MinItems = Field(Int, Shacl + "minCount")

  val MaxItems = Field(Int, Shacl + "maxCount")

  val UniqueItems = Field(Bool, Shapes + "uniqueItems")

  val CollectionFormat = Field(Str, Shapes + "collectionFormat")

  override val fields
    : List[Field] = List(Items, MinItems, MaxItems, UniqueItems, CollectionFormat) ++ AnyShapeModel.fields ++ DomainElementModel.fields
}

object ArrayShapeModel extends DataArrangementShape with DomainElementModel {

  override val `type`: List[ValueType] = List(Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = ArrayShape()
}

object MatrixShapeModel extends DataArrangementShape with DomainElementModel {
  override val `type`
    : List[ValueType]        = List(Shapes + "MatrixShape", Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
  override def modelInstance = MatrixShape()
}

object TupleShapeModel extends DataArrangementShape with DomainElementModel {
  override val Items = Field(SortedArray(ShapeModel), Shapes + "items")
  override val `type`
    : List[ValueType]        = List(Shapes + "TupleShape", Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
  override def modelInstance = TupleShape()
}
