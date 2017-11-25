package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.plugins.domain.shapes.models.FileShape
import amf.core.vocabulary.{Namespace, ValueType}

object FileShapeModel extends ShapeModel with DomainElementModel with CommonShapeFields {

  val FileTypes = Field(Array(Str), Namespace.Shapes + "fileType")

  override def fields: List[Field] =
    List(FileTypes) ++ commonOASFields ++ ShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "FileShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = FileShape()
}
