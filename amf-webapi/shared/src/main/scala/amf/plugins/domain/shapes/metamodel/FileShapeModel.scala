package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.FileShape

object FileShapeModel extends AnyShapeModel with CommonShapeFields {

  val FileTypes = Field(Array(Str), Namespace.Shapes + "fileType")

  val specificFields = List(FileTypes)
  override def fields: List[Field] =
    specificFields ++ commonOASFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "FileShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = FileShape()
}
