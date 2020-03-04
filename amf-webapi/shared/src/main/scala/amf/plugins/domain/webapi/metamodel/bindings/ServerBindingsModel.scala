package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.vocabulary.ValueType
import amf.core.vocabulary.Namespace.{ApiBinding, ApiContract}
import amf.plugins.domain.webapi.models.bindings.ServerBindings

object ServerBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiContract + "ServerBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(ServerBindingModel),
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "List of server bindings")
  )

  override def fields: List[Field] =
    List(
      Name,
      Bindings,
    ) ++ DomainElementModel.fields

  override def modelInstance = ServerBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "ServerBindings",
    ""
  )
}

