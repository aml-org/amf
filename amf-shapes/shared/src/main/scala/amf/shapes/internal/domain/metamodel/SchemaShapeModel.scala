package amf.shapes.internal.domain.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain._
import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.shapes.client.scala.domain.models.SchemaShape

trait SchemaShapeModel extends AnyShapeModel with ExternalSourceElementModel {
  val MediaType =
    Field(Str, Core + "mediaType", ModelDoc(ModelVocabularies.Core, "mediaType", "Media type associated to a shape"))

  val specificFields = List(MediaType)
  override val fields: List[Field] = specificFields ++
    AnyShapeModel.fields ++
    DomainElementModel.fields

  override val `type`: List[ValueType] = List(Shapes + "SchemaShape") ++ AnyShapeModel.`type`

  override def modelInstance = SchemaShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "SchemaShape",
    "Raw schema that cannot be parsed using AMF shapes model"
  )
}

object SchemaShapeModel extends SchemaShapeModel
