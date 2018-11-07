package amf.plugins.domain.shapes.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.domain._
import amf.core.vocabulary.Namespace.{Shacl, Shapes}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.shapes.metamodel.common.{DocumentationField, ExamplesField}
import amf.plugins.domain.shapes.models.AnyShape

trait AnyShapeModel extends ShapeModel with ExternalSourceElementModel with ExamplesField with DocumentationField {

  val XMLSerialization = Field(
    XMLSerializerModel,
    Shapes + "xmlSerialization",
    ModelDoc(ModelVocabularies.Shapes, "XML serialization", "information about how to serialize"))

  override def fields: List[Field] =
    ShapeModel.fields ++ ExternalSourceElementModel.fields ++ List(Documentation, XMLSerialization, Examples)

  override val `type`: List[ValueType] =
    List(Shapes + "AnyShape", Shacl + "Shape", Shapes + "Shape")

  override def modelInstance = AnyShape()
}

object AnyShapeModel extends AnyShapeModel {
  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Any Shape",
    "Base class for all shapes stored in the graph model"
  )
}
