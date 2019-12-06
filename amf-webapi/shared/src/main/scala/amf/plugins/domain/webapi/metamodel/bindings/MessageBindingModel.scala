package amf.plugins.domain.webapi.metamodel.bindings

import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.vocabulary.Namespace.ApiContract
import amf.core.vocabulary.ValueType

trait MessageBindingModel extends DomainElementModel

object MessageBindingModel extends MessageBindingModel {
  override def modelInstance           = throw new Exception("MessageBinding is an abstract class")
  override def fields: List[Field]     = DomainElementModel.fields
  override val `type`: List[ValueType] = ApiContract + "MessageBinding" :: DomainElementModel.`type`
}
