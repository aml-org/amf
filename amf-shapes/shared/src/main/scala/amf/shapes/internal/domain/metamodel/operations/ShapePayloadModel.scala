package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.shapes.client.scala.model.domain.operations.ShapePayload

object ShapePayloadModel extends AbstractPayloadModel {

  override val key: Field = Name

  override val `type`: List[ValueType] = Shapes + "Payload" :: Core + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Schema :: MediaType :: Examples :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Shapes,
    "ShapePayload",
    "Encoded payload using certain media-type"
  )

  override def modelInstance: ShapePayload = ShapePayload()

}
