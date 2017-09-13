package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.{Shacl, Shapes}
import amf.metadata.Type.{Bool, Int}
import amf.vocabulary.ValueType

/**
  * Array shape metamodel
  */
object ArrayShapeModel extends ShapeModel with DomainElementModel {

  val Items = Field(ShapeModel, Shapes + "items")

  val MinItems = Field(Int, Shacl + "minCount")

  val MaxItems = Field(Int, Shacl + "maxCount")

  val UniqueItems = Field(Bool, Shapes + "uniqueItems")


  override val fields: List[Field] = List(Items,
                                          MinItems,
                                          MaxItems,
                                          UniqueItems) ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "ArrayShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}
