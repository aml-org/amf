package amf.plugins.domain.apicontract.metamodel

import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type.Str
import amf.core.internal.metamodel.domain.{DomainElementModel, ModelDoc, ModelVocabularies}
import amf.core.internal.metamodel.domain.common.NameFieldSchema
import amf.core.internal.metamodel.domain.templates.KeyField
import amf.plugins.domain.apicontract.models.Callback
import amf.core.client.scala.vocabulary.Namespace.ApiContract
import amf.core.client.scala.vocabulary.ValueType

/**
  * Callback metaModel.
  */
object CallbackModel extends DomainElementModel with KeyField with NameFieldSchema {

  val Expression = Field(Str,
                         ApiContract + "expression",
                         ModelDoc(ModelVocabularies.ApiContract,
                                  "expression",
                                  "Structural location of the information to fulfill the callback"))

  val Endpoint = Field(EndPointModel,
                       ApiContract + "endpoint",
                       ModelDoc(ModelVocabularies.ApiContract, "endpoint", "Endpoint targeted by the callback"))

  override val `type`: List[ValueType] = ApiContract + "Callback" :: DomainElementModel.`type`

  override val fields: List[Field] = Name :: Expression :: Endpoint :: DomainElementModel.fields

  override def modelInstance = Callback()

  override val key: Field = Name

  override val doc: ModelDoc = ModelDoc(
    ModelVocabularies.ApiContract,
    "Callback",
    "Model defining the information for a HTTP callback/ webhook"
  )
}
