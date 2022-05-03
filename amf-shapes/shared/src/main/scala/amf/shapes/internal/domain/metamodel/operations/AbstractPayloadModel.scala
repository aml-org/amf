package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.shapes.internal.domain.metamodel.common.ExamplesField

trait AbstractPayloadModel
    extends DomainElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField
    with ExamplesField {

  val MediaType: Field = Field(
    Str,
    Core + "mediaType",
    ModelDoc(ModelVocabularies.Core, "mediaType", "Media types supported in the payload")
  )

  val Schema: Field =
    Field(
      ShapeModel,
      Shapes + "schema",
      ModelDoc(ModelVocabularies.Shapes, "schema", "Schema associated to this payload")
    )

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Schema :: MediaType :: Examples :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "AbstractPayload",
    "Encoded payload using certain media-type"
  )
}

object AbstractPayloadModel extends AbstractPayloadModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractPayloadModel is an abstract class")
}
