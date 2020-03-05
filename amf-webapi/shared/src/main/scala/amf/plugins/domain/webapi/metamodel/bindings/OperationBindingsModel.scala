package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.Type.Array
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.vocabulary.ValueType
import amf.core.vocabulary.Namespace.{ApiBinding, ApiContract}
import amf.plugins.domain.webapi.models.bindings.OperationBindings

object OperationBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiContract + "OperationBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(OperationBindingModel),
    ApiBinding + "binding",
    ModelDoc(ModelVocabularies.ApiBinding, "binding", "List of operation bindings")
  )

  override def fields: List[Field] =
    List(
      Name,
      Bindings,
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = OperationBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "OperationBindings",
    ""
  )
}
