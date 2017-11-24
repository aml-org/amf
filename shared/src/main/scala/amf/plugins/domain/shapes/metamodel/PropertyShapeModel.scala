package amf.plugins.domain.shapes.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.{Int, Iri}
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.PropertyShape
import amf.framework.vocabulary.Namespace.{Shacl, Shapes}
import amf.framework.vocabulary.ValueType

/**
  * Property shape metamodel
  */
object PropertyShapeModel extends ShapeModel {

  val Path = Field(Iri, Shacl + "path")

  val Range = Field(ShapeModel, Shapes + "range")

  val MinCount = Field(Int, Shacl + "minCount")

  val MaxCount = Field(Int, Shacl + "maxCount")

  override val `type`: List[ValueType] = List(Shacl + "PropertyShape") ++ ShapeModel.`type`

  override def fields: List[Field] =
    List(Path, Range, MinCount, MaxCount) ++ ShapeModel.fields ++ DomainElementModel.fields

  override def modelInstance = PropertyShape()
}
