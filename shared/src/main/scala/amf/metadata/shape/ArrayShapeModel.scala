package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Shacl, Shapes}
import amf.metadata.Type.{Bool, Int, SortedArray}
import amf.vocabulary.ValueType

/**
  * Array shape metamodel
  */

/**
  * Common fields to all arrays, matrix and tuples
  */
trait DataArrangementShape {
  val Items = Field(ShapeModel, Shapes + "items")

  val MinItems = Field(Int, Shacl + "minCount")

  val MaxItems = Field(Int, Shacl + "maxCount")

  val UniqueItems = Field(Bool, Shapes + "uniqueItems")

  val fields: List[Field] = List(Items,
    MinItems,
    MaxItems,
    UniqueItems) ++ ShapeModel.fields ++ DomainElementModel.fields
}

object ArrayShapeModel extends DataArrangementShape with  ShapeModel with DomainElementModel {

  override val `type`: List[ValueType] = List(Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

}

object MatrixShapeModel extends DataArrangementShape with  ShapeModel with DomainElementModel {
  override val `type`: List[ValueType] = List(Shapes + "MatrixShape", Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}

object TupleShapeModel extends DataArrangementShape with  ShapeModel with DomainElementModel {
  override val Items = Field(SortedArray(ShapeModel), Shapes + "items")
  override val `type`: List[ValueType] = List(Shapes + "TupleShape", Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}

