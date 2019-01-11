package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.FileShape

object FileShapeModel extends AnyShapeModel with CommonShapeFields {

  val FileTypes = Field(Array(Str),
                        Namespace.Shapes + "fileType",
                        ModelDoc(ModelVocabularies.Shapes, "file type", "Type of file described by this shape"))

  val specificFields = List(FileTypes)
  override def fields: List[Field] =
    specificFields ++ commonOASFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "FileShape", Namespace.Shacl + "Shape", Namespace.Shapes + "Shape")

  override def modelInstance = FileShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "File Shape",
    "Shape describing data uploaded in an API request"
  )
}
