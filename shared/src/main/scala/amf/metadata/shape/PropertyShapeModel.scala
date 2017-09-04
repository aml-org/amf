package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Int, Iri}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.Namespace.Shacl
import amf.vocabulary.ValueType

/**
  * Created by pedro.colunga on 9/4/17.
  */
object PropertyShapeModel extends ShapeModel {

  val Path = Field(Iri, Shacl + "path")

  val DataType = Field(Iri, Shacl + "datatype")

  val MinCount = Field(Int, Shacl + "minCount")

  val MaxCount = Field(Int, Shacl + "maxCount")

  override val `type`: List[ValueType] = List(Shacl + "PropertyShape") ++ ShapeModel.`type`

  override val fields
    : List[Field] = List(Path, DataType, MinCount, MaxCount) ++ ShapeModel.fields ++ DomainElementModel.fields
}
