package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{Core, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.{DocumentationField, ExamplesField}
import amf.plugins.domain.shapes.models.AnyShape

trait AnyShapeModel extends ShapeModel with ExternalSourceElementModel with ExamplesField with DocumentationField {

  val XMLSerialization = Field(
    XMLSerializerModel,
    Shapes + "xmlSerialization",
    ModelDoc(ModelVocabularies.Shapes, "XML serialization", "Information about how to serialize"))

  val Comment =
    Field(Str,
          Core + "comment",
          ModelDoc(ModelVocabularies.Core,
                   "comment",
                   "A comment on an item. The comment's content is expressed via the text"))

  override val `type`: List[ValueType] =
    List(Shapes + "AnyShape") ++ ShapeModel.`type`

  override def modelInstance = AnyShape()
}

object AnyShapeModel extends AnyShapeModel {

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Any Shape",
    "Base class for all shapes stored in the graph model",
    superClasses = Seq((Shapes + "Shape").iri())
  )

  override val fields: List[Field] =
    ShapeModel.fields ++ List(Documentation, XMLSerialization, Comment, Examples) ++ ExternalSourceElementModel.fields
}
