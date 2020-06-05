package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.{DynamicBinding, EmptyBinding}

object DynamicBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel {

  val Definition = Field(
    DataNodeModel,
    ApiBinding + "definition",
    ModelDoc(ModelVocabularies.ApiBinding, "definition", "definition of the unknown dynamic binding"))

  override def fields: List[Field] = List(Definition, Type) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "DynamicBinding" :: DomainElementModel.`type`

  override def modelInstance: AmfObject = DynamicBinding()

  override val key: Field = Type

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "DynamicBinding",
    ""
  )
}

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
