package amf.shapes.internal.domain.metamodel.`abstract`

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.{Core, Shapes}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain._
import amf.core.internal.metamodel.domain.common.{DescriptionField, NameFieldSchema}
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.shapes.client.scala.model.domain.operations.AbstractPayload
import amf.shapes.internal.domain.metamodel.common.ExamplesField

trait AbstractPayloadModel
    extends DomainElementModel
    with KeyField
    with NameFieldSchema
    with DescriptionField
    with ExamplesField {

  val Schema =
    Field(ShapeModel,
          Core + "schema",
          ModelDoc(ModelVocabularies.Core, "schema", "Schema associated to this payload"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Payload" :: DomainElementModel.`type`

  override val fields: List[Field] =
    Name :: Schema :: Examples :: (DomainElementModel.fields ++ LinkableElementModel.fields)

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "Payload",
    "Encoded payload using certain media-type"
  )
}

object AbstractPayloadModel extends AbstractPayloadModel {
  override def modelInstance: AmfObject =  throw new Exception("AbstractPayloadModel is an abstract class")
}
