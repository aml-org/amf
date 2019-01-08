package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.Shacl
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.models.SchemaShape

object SchemaShapeModel extends AnyShapeModel with ExternalSourceElementModel {
  val MediaType = Field(Str,
                        Shacl + "mediaType",
                        ModelDoc(ExternalModelVocabularies.Shacl, "media type", "Media type associated to a shape"))

  val specificFields = List(MediaType)
  override val fields: List[Field] = specificFields ++
    AnyShapeModel.fields ++
    DomainElementModel.fields ++
    LinkableElementModel.fields

  override val `type`: List[ValueType] = List(Shacl + "SchemaShape") ++ ShapeModel.`type` ++ DomainElementModel.`type`

  override def modelInstance = SchemaShape()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Schema Shape",
    "Raw schema that cannot be parsed using AMF shapes model"
  )
}
