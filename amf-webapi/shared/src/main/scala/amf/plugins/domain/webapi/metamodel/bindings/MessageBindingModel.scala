package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType

trait MessageBindingModel extends DomainElementModel with BindingType with KeyField

object MessageBindingModel extends MessageBindingModel {

  override def modelInstance           = throw new Exception("MessageBinding is an abstract class")
  override def fields: List[Field]     = List(Type) ++ DomainElementModel.fields
  override val `type`: List[ValueType] = ApiBinding + "MessageBinding" :: DomainElementModel.`type`

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "MessageBinding",
    ""
  )
}
