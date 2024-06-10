package amf.shapes.internal.domain.metamodel

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.{Array, Str}
import amf.core.internal.metamodel.domain.{ExternalSourceElementModel, ModelDoc, ModelVocabularies, ShapeModel}
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.domain.metamodel.avro.AvroFields
import amf.shapes.internal.domain.metamodel.common.{DocumentationField, ExamplesField}

trait AnyShapeModel
    extends ShapeModel
    with ExternalSourceElementModel
    with ExamplesField
    with DocumentationField
    with WithSemanticContext
    with AvroFields {

  val XMLSerialization: Field = Field(
    XMLSerializerModel,
    Shapes + "xmlSerialization",
    ModelDoc(ModelVocabularies.Shapes, "XmlSerialization", "Information about how to serialize")
  )

  val Comment: Field =
    Field(
      Str,
      Core + "comment",
      ModelDoc(
        ModelVocabularies.Core,
        "comment",
        "A comment on an item. The comment's content is expressed via the text"
      )
    )

  override val `type`: List[ValueType] =
    List(Shapes + "AnyShape") ++ ShapeModel.`type`

  override def modelInstance: AnyShape = AnyShape()
}

object AnyShapeModel extends AnyShapeModel {

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "AnyShape",
    "Base class for all shapes stored in the graph model",
    superClasses = Seq((Shapes + "Shape").iri())
  )

  override val fields: List[Field] =
    ShapeModel.fields ++ List(
      Documentation,
      XMLSerialization,
      Comment,
      Examples,
      AvroNamespace,
      Aliases,
      Size
    ) ++ ExternalSourceElementModel.fields
}
