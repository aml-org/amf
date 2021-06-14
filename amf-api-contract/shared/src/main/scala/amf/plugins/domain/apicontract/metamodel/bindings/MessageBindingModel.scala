package amf.plugins.domain.apicontract.metamodel.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.templates.KeyField

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
