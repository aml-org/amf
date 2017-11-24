package amf.plugins.domain.webapi.metamodel

import amf.framework.metamodel.Field
import amf.framework.metamodel.Type.Str
import amf.framework.metamodel.domain.DomainElementModel
import amf.plugins.domain.webapi.models.ExternalDomainElement
import amf.framework.vocabulary.Namespace._
import amf.framework.vocabulary.ValueType

object ExternalDomainElementModel extends DomainElementModel {

  val Raw = Field(Str, Document + "raw")

  override def fields: List[Field] = List(Raw)

  override val `type`: List[ValueType] = Document + "ExternalDomainElement" :: DomainElementModel.`type`

  override def modelInstance = ExternalDomainElement()
}
