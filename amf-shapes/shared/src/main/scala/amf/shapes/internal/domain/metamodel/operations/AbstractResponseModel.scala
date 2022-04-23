package amf.shapes.internal.domain.metamodel.operations

import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.Core
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

trait AbstractResponseModel extends DomainElementModel with KeyField with NameFieldSchema {

  val Payload: Field = Field(AbstractPayloadModel,
                             Core + "payload",
                             ModelDoc(ModelVocabularies.Core, "payload", "Payload for a Request/Response"))

  override val key: Field = Name

  override val `type`: List[ValueType] = Core + "Response" :: DomainElementModel.`type`

  override val fields: List[Field] = List(Name) :+ Payload

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.Core,
    "AbstractResponse",
    "Response information for an operation"
  )
}

object AbstractResponseModel extends AbstractResponseModel {
  override def modelInstance: AmfObject = throw new Exception("AbstractResponseModel is an abstract class")
}
