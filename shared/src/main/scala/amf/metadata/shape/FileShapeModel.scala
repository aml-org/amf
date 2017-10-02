package amf.metadata.shape

import amf.metadata.Field
import amf.metadata.Type.{Str, Array}
import amf.metadata.domain.DomainElementModel
import amf.vocabulary.{Namespace, ValueType}

object FileShapeModel extends ShapeModel with DomainElementModel with CommonOASFields {

  val FileTypes = Field(Array(Str), Namespace.Shapes + "fileType")

  override def fields: List[Field] =
    List(FileTypes) ++ commonOASFields ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "FileShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")
}
