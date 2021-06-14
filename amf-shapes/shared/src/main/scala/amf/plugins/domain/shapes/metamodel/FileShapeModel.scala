package amf.plugins.domain.shapes.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{DomainElementModel, ExternalModelVocabularies, ModelDoc, ModelVocabularies}
import amf.core.client.scala.vocabulary.{Namespace, ValueType}
import amf.plugins.domain.shapes.models.FileShape

object FileShapeModel extends AnyShapeModel with CommonShapeFields {

  val FileTypes = Field(Array(Str),
                        Namespace.Shapes + "fileType",
                        ModelDoc(ModelVocabularies.Shapes, "fileType", "Type of file described by this shape"))

  val specificFields = List(FileTypes)
  override val fields: List[Field] =
    specificFields ++ commonOASFields ++ AnyShapeModel.fields ++ DomainElementModel.fields

  override val `type`: List[ValueType] =
    List(Namespace.Shapes + "FileShape") ++ AnyShapeModel.`type`

  override def modelInstance = FileShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "FileShape",
    "Shape describing data uploaded in an API request"
  )
}
