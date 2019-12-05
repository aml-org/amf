package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

trait ChannelBindingModel extends DomainElementModel {
  override def modelInstance: AmfObject = ???
  override def fields: List[Field]      = DomainElementModel.fields
  override val `type`: List[ValueType]  = ApiContract + "ChannelBinding" :: DomainElementModel.`type`
}

object ChannelBindingModel extends ChannelBindingModel
