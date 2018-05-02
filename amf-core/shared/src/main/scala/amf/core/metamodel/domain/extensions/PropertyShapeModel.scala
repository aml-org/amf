package amf.core.metamodel.domain.extensions

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Bool, Int, Iri}
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.model.domain.extensions.PropertyShape
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType

/**
  * Property shape metamodel
  *
  * Model for SHACL PropertyShapes
  */
object PropertyShapeModel extends ShapeModel {

  val Path = Field(Iri, Shacl + "path")

  val Range = Field(ShapeModel, Shapes + "range")

  val MinCount = Field(Int, Shacl + "minCount")

  val MaxCount = Field(Int, Shacl + "maxCount")

  val ReadOnly = Field(Bool, Shapes + "readOnly")

  val WriteOnly = Field(Bool, Shapes + "writeOnly")

  val Deprecated = Field(Bool, Shapes + "deprecated")

  override val `type`: List[ValueType] = List(Shacl + "PropertyShape") ++ ShapeModel.`type`

  override def fields: List[Field] =
    List(Path, Range, MinCount, MaxCount, ReadOnly) ++ ShapeModel.fields ++ DomainElementModel.fields

  override def modelInstance = PropertyShape()
}
