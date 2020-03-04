package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType

trait MessageBindingModel extends DomainElementModel

object MessageBindingModel extends MessageBindingModel with NameFieldSchema {
  override def modelInstance           = throw new Exception("MessageBinding is an abstract class")
  override def fields: List[Field]     = Name :: DomainElementModel.fields
  override val `type`: List[ValueType] = ApiBinding + "MessageBinding" :: DomainElementModel.`type`

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MessageBinding",
    ""
  )
}
