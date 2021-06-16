package amf.apicontract.internal.metamodel.domain.bindings

import amf.core.client.scala.vocabulary.Namespace.ApiBinding
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.{DomainElementModel, LinkableElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.apicontract.client.scala.model.domain.bindings.OperationBindings
import amf.core.internal.metamodel.Type.Array


object OperationBindingsModel extends DomainElementModel with NameFieldSchema {

  override val `type`: List[ValueType] = ApiBinding + "OperationBindings" :: DomainElementModel.`type`

  val Bindings = Field(
    Array(OperationBindingModel),
    ApiBinding + "bindings",
    ModelDoc(ModelVocabularies.ApiBinding, "bindings", "List of operation bindings")
  )

  override def fields: List[Field] =
    List(
      Name,
      Bindings,
    ) ++ LinkableElementModel.fields ++ DomainElementModel.fields

  override def modelInstance = OperationBindings()

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiBinding,
    "OperationBindings",
    ""
  )
}
