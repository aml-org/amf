package amf.plugins.domain.webapi.metamodel.bindings
import amf.core.metamodel.Field
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.domain.AmfObject
import amf.core.vocabulary.Namespace.ApiBinding
import amf.core.vocabulary.ValueType

trait ServerBindingModel extends DomainElementModel

object ServerBindingModel extends ServerBindingModel {
  override def modelInstance           = throw new Exception("ServerBinding is an abstract class")
  override def fields: List[Field]     = DomainElementModel.fields
  override val `type`: List[ValueType] = ApiBinding + "ServerBinding" :: DomainElementModel.`type`
}
