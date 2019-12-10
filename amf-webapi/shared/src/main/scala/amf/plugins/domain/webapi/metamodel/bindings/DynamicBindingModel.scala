package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{ModelDoc, ModelVocabularies, DataNodeModel, DomainElementModel}
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.bindings.{DynamicBinding, EmptyBinding}

object DynamicBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel
    with KeyField {

  val Definition = Field(
    DataNodeModel,
    ApiBinding + "definition",
    ModelDoc(ModelVocabularies.ApiBinding, "definition", "definition of the unknown dynamic binding"))

  val Type = Field(Str,
                   ApiBinding + "type",
                   ModelDoc(ModelVocabularies.ApiBinding, "type", "type that the binding is defining"))

  override def fields: List[Field] = List(Definition) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiBinding + "DynamicBinding" :: DomainElementModel.`type`

  override val key: Field = Type

  override def modelInstance: AmfObject = DynamicBinding()
}

/** This model exists to express that this binding definition MUST be empty (have no definition) */
object EmptyBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel {
  val Type = Field(Str,
                   ApiBinding + "type",
                   ModelDoc(ModelVocabularies.ApiBinding, "type", "empty binding for a corresponding known type"))

  override val `type`: List[ValueType] = ApiBinding + "EmptyBinding" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Type)

  override def modelInstance: AmfObject = EmptyBinding()
}
