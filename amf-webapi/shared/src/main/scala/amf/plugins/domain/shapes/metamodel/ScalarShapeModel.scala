package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Iri
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.ScalarShape

/**
  * Scalar shape metamodel
  */
object ScalarShapeModel extends AnyShapeModel with CommonShapeFields {

  val DataType = Field(Iri, Shacl + "datatype")

  val specificFields = List(DataType)
  override val fields
    : List[Field] = specificFields ++ commonOASFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "ScalarShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = ScalarShape()
}