package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Str
import amf.core.metamodel.domain.templates.KeyField
import amf.core.metamodel.domain.{DataNodeModel, DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

object DynamicBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel
    with KeyField {
  val Definition = Field(
    DataNodeModel,
    ApiContract + "definition",
    ModelDoc(ModelVocabularies.ApiContract, "definition", "definition of the unknown dynamic binding"))

  val Type = Field(Str,
                   ApiContract + "type",
                   ModelDoc(ModelVocabularies.ApiContract, "type", "type that the binding is defining"))

  override def fields: List[Field] = List(Definition) ++ DomainElementModel.fields

  override val `type`: List[ValueType] = ApiContract + "DynamicBinding" :: DomainElementModel.`type`

  override val key: Field = Type
}

/** This model exists to express that this binding definition MUST be empty (have no definition) */
object EmptyBindingModel
    extends ServerBindingModel
    with ChannelBindingModel
    with OperationBindingModel
    with MessageBindingModel {
  val Type = Field(Str,
                   ApiContract + "type",
                   ModelDoc(ModelVocabularies.ApiContract, "type", "empty binding for a corresponding known type"))

  override val `type`: List[ValueType] = ApiContract + "EmptyBinding" :: DomainElementModel.`type`

  override def fields: List[Field] = List(Type)
}
