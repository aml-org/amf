package amf.plugins.domain.webapi.metamodel

import amf.core.metamodel.Field
import amf.core.metamodel.Type.{Array, Str}
import amf.core.metamodel.domain.DomainElementModel
import amf.core.metamodel.domain.common.NameFieldSchema
import amf.core.metamodel.domain.templates.KeyField
import amf.core.vocabulary.Namespace.{Http, Schema}
import amf.core.vocabulary.ValueType
import amf.plugins.domain.webapi.models.Callback

/**
  * Callback metaModel.
  */
object CallbackModel extends DomainElementModel with KeyField with NameFieldSchema {

  val Expression = Field(Str, Http + "expression")

  val Endpoint = Field(EndPointModel, Http + "endpoint")

  override val `type`: List[ValueType] = Http + "Callback" :: DomainElementModel.`type`

  override def fields: List[Field] = Name :: Expression :: Endpoint :: DomainElementModel.fields

  override def modelInstance = Callback()

  override val key: Field = Name
}
