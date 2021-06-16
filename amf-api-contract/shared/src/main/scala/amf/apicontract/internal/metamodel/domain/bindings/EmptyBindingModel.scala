package amf.apicontract.internal.metamodel.domain.bindings

import amf.apicontract.client.scala.model.domain.bindings.EmptyBinding
import amf.core.client.scala.model.domain.AmfObject
import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}

/** This model exists to express that this binding definition MUST be empty (have no definition) */
object EmptyBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel {

  override val `type`: List[ValueType] = ApiBinding + "EmptyBinding" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Type)

  override def modelInstance: AmfObject = EmptyBinding()

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "EmptyBinding",
    ""
  )
}
