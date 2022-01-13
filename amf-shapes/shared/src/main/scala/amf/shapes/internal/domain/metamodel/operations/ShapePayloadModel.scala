package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.Shapes
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain._
import amf.shapes.client.scala.model.domain.operations.ShapePayload
import amf.shapes.internal.domain.metamodel.common.ExamplesField

object ShapePayloadModel
    extends DomainElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField
    with ExamplesField {

  val Schema =
    Field(ShapeModel,
          Shapes + "schema",
          ModelDoc(ModelVocabularies.Shapes, "schema", "Schema associated to this payload"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Schema :: Examples :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override def modelInstance = ShapePayload()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "Payload",
    "Encoded payload using certain media-type"
  )
}
