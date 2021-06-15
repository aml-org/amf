package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{ExternalSourceElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.shapes.internal.domain.metamodel.common.{DocumentationField, ExamplesField}
import amf.shapes.client.scala.domain.models.AnyShape

trait AnyShapeModel extends ShapeModel with ExternalSourceElementModel with ExamplesField with DocumentationField {

  val XMLSerialization = Field(
    XMLSerializerModel,
    Shapes + "xmlSerialization",
    ModelDoc(ModelVocabularies.Shapes, "XmlSerialization", "Information about how to serialize"))

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
    "AnyShape",
    "Base class for all shapes stored in the graph model",
    superClasses = Seq((Shapes + "Shape").iri())
  )

  override val fields: List[Field] =
    ShapeModel.fields ++ List(Documentation, XMLSerialization, Comment, Examples) ++ ExternalSourceElementModel.fields
}
