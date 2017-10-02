package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.Iri
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Shacl
import amf.vocabulary.ValueType

/**
  * Scalar shape metamodel
  */
object ScalarShapeModel extends ShapeModel with DomainElementModel with CommonOASFields {

  val DataType = Field(Iri, Shacl + "datatype")

  override val fields: List[Field] = List(DataType) ++ commonOASFields ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "ScalarShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`
}
